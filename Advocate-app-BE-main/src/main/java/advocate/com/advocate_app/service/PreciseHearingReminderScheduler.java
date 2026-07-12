package advocate.com.advocate_app.service;

import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.communication.service.CommunicationDispatcher;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.CaseEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PreciseHearingReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(PreciseHearingReminderScheduler.class);

    private static final long TWENTY_FOUR_HOURS_MS = 24L * 60 * 60 * 1000;
    private static final long TWO_HOURS_MS = 2L * 60 * 60 * 1000;

    private static final long WINDOW_24H_MS = 30L * 60 * 1000;
    private static final long WINDOW_2H_MS = 15L * 60 * 1000;

    private final ConcurrentHashMap<Long, Boolean> reminded24h = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Boolean> reminded2h = new ConcurrentHashMap<>();

    @Autowired
    private CaseEventRepository caseEventRepository;

    @Autowired
    private CommunicationDispatcher notificationDispatcher;

    @Autowired
    private EmailTemplateService templateService;

    @Scheduled(fixedRate = 1800000)
    public void checkPreciseReminders() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<CaseEventEntity> events = caseEventRepository.findEventsForTodayAndTomorrow(today, tomorrow);
        if (events.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (CaseEventEntity event : events) {
            try {
                if (event.getDate() == null || event.getTime() == null) continue;

                LocalDateTime eventDateTime = LocalDateTime.of(event.getDate(), event.getTime());
                long diffMs = Duration.between(now, eventDateTime).toMillis();

                if (diffMs <= 0) continue;

                Advocate advocate = event.getAdvocate();
                CaseEntity caseEntity = event.getCaseEntity();
                if (advocate == null || caseEntity == null) continue;

                Client client = caseEntity.getClient();

                if (diffMs >= TWENTY_FOUR_HOURS_MS - WINDOW_24H_MS
                        && diffMs <= TWENTY_FOUR_HOURS_MS + WINDOW_24H_MS
                        && reminded24h.putIfAbsent(event.getId(), Boolean.TRUE) == null) {
                    sendReminder(event, client, advocate, caseEntity, "24-hour");
                }

                if (diffMs >= TWO_HOURS_MS - WINDOW_2H_MS
                        && diffMs <= TWO_HOURS_MS + WINDOW_2H_MS
                        && reminded2h.putIfAbsent(event.getId(), Boolean.TRUE) == null) {
                    sendReminder(event, client, advocate, caseEntity, "2-hour");
                }

            } catch (Exception e) {
                log.warn("Error processing precise reminder for event {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    private void sendReminder(CaseEventEntity event, Client client,
                              Advocate advocate, CaseEntity caseEntity, String label) {
        if (client == null) {
            log.warn("No client for event {} — skipping {} reminder", event.getId(), label);
            return;
        }

        String formattedDate = event.getDate() != null ? event.getDate().toString() : "TBD";
        String formattedTime = event.getTime() != null ? event.getTime().toString() : "TBD";

        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("1", client.getName());
        templateParams.put("2", caseEntity.getCaseNumber());
        templateParams.put("3", formattedDate);
        templateParams.put("4", formattedTime);

        NotificationPayload payload = new NotificationPayload();
        payload.setType(NotificationType.HEARING_REMINDER);
        payload.setRecipientName(client.getName());
        payload.setRecipientEmail(client.getEmail());
        payload.setRecipientPhone(client.getPhone());
        payload.setAdvocate(advocate);
        payload.setCaseEntity(caseEntity);
        payload.setClient(client);
        payload.setSubject(label + " Hearing Reminder — " + caseEntity.getCaseNumber());
        payload.setEmailBody(templateService.hearingReminderEmail(client.getName(),
                caseEntity.getCaseNumber(), event.getDate(), event.getTime(),
                caseEntity.getCourtLevel(), "/cases/" + caseEntity.getId()));
        payload.setWhatsappMessage(templateService.hearingReminderWhatsApp(client.getName(),
                caseEntity.getCaseNumber(), event.getDate(), event.getTime(),
                caseEntity.getCourtLevel()));
        payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_HEARING_REMINDER);
        payload.setWhatsappTemplateParameters(templateParams);
        notificationDispatcher.dispatchSafely(payload);

        log.info("{} reminder sent for event {} | case={} | client={}", label, event.getId(),
                caseEntity.getCaseNumber(), client.getEmail());
    }
}
