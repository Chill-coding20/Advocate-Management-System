package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.communication.service.CommunicationDispatcher;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CaseEventService {

    private static final Logger log = LoggerFactory.getLogger(CaseEventService.class);

    @Autowired
    private CaseEventRepository caseEventRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private NotificationService notificationService; // ✅ In-app notifications (preserved)

    @Autowired
    private CommunicationDispatcher notificationDispatcher;

    @Autowired
    private EmailTemplateService templateService;

    @Autowired
    private CaseTimelineService timelineService;

    @Autowired
    private AuditLogService auditLogService;

    public Page<CaseEventEntity> getEventsPaged(String email, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return caseEventRepository.findByAdvocate(advocate, pageable);
    }

    // ✅ Create new event (and trigger notification)
    public CaseEventEntity createEvent(String advocateEmail, CaseEventEntity event) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        CaseEntity caseEntity = caseRepository.findById(event.getCaseEntity().getId())
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (!caseEntity.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to create event for this case");
        }

        event.setAdvocate(advocate);
        event.setCaseEntity(caseEntity);
        event.setNotified(false);

        CaseEventEntity savedEvent = caseEventRepository.save(event);

        // ✅ Create in-app notification (preserved existing behavior)
        String message = "New " + event.getEventType() + " scheduled for case: "
                + caseEntity.getCaseTitle() + " on " + event.getDate();
        notificationService.createNotification(message, advocate);

        // ✅ Send email + WhatsApp to client
        try {
            Client client = caseEntity.getClient();
            if (client != null) {
                String formattedDate = event.getDate() != null ? event.getDate().toString() : "TBD";
                String formattedTime = event.getTime() != null ? event.getTime().toString() : "TBD";
                Map<String, String> templateParams = new HashMap<>();
                templateParams.put("1", client.getName());
                templateParams.put("2", caseEntity.getCaseNumber());
                templateParams.put("3", formattedDate);
                templateParams.put("4", formattedTime);

                NotificationPayload payload = new NotificationPayload();
                payload.setType(NotificationType.HEARING_SCHEDULED);
                payload.setRecipientName(client.getName());
                payload.setRecipientEmail(client.getEmail());
                payload.setRecipientPhone(client.getPhone());
                payload.setAdvocate(advocate);
                payload.setCaseEntity(caseEntity);
                payload.setClient(client);
                payload.setSubject(event.getEventType() + " Scheduled — " + caseEntity.getCaseNumber());
                payload.setEmailBody(templateService.hearingScheduledEmail(client.getName(),
                        caseEntity.getCaseNumber(), event.getDate(), event.getTime(),
                        caseEntity.getCourtLevel(), event.getEventType(), "/cases/" + caseEntity.getId()));
                payload.setWhatsappMessage(templateService.hearingReminderWhatsApp(client.getName(),
                        caseEntity.getCaseNumber(), event.getDate(), event.getTime(),
                        caseEntity.getCourtLevel()));
                payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_HEARING_REMINDER);
                payload.setWhatsappTemplateParameters(templateParams);
                notificationDispatcher.dispatchSafely(payload);
            }
        } catch (Exception e) {
            log.warn("Could not dispatch HEARING_SCHEDULED notification: {}", e.getMessage());
        }

        // Record timeline
        try {
            String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String dateStr = savedEvent.getDate() != null ? savedEvent.getDate().toString() : "";
            String timeStr = savedEvent.getTime() != null ? savedEvent.getTime().toString() : "";
            timelineService.recordEvent(
                    caseEntity.getId(), caseEntity.getClient() != null ? caseEntity.getClient().getId() : null,
                    advocate.getId(), CaseTimelineService.HEARING_CREATED,
                    savedEvent.getEventType() + " Scheduled",
                    (savedEvent.getTitle() != null ? savedEvent.getTitle() + " — " : "") + dateStr + (timeStr.isEmpty() ? "" : " " + timeStr),
                    performedBy, "Hearing", savedEvent.getId()
            );
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.HEARING_CREATED, AuditLogService.MODULE_HEARINGS,
                    savedEvent.getEventType() + " Scheduled",
                    (savedEvent.getTitle() != null ? savedEvent.getTitle() + " — " : "") +
                            (savedEvent.getDate() != null ? savedEvent.getDate().toString() : ""),
                    "Hearing", savedEvent.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        return savedEvent;
    }

    // ✅ Get all events for logged-in advocate
    public List<CaseEventEntity> getMyEvents(String advocateEmail) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return caseEventRepository.findByAdvocate(advocate);
    }

    // ✅ Get events for a particular date (like today)
    public List<CaseEventEntity> getEventsByDate(String advocateEmail, LocalDate date) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return caseEventRepository.findByAdvocateAndDate(advocate, date);
    }

    // ✅ Get upcoming events (next 7 days)
    public List<CaseEventEntity> getUpcomingEvents(String advocateEmail) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return caseEventRepository.findUpcomingEvents(advocate, today, nextWeek);
    }

    // ✅ Update event (reschedule) — sends HEARING_RESCHEDULED notification
    public CaseEventEntity updateEvent(String advocateEmail, Long eventId, CaseEventEntity updated) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        CaseEventEntity existing = caseEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!existing.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to update this event");
        }

        LocalDate oldDate = existing.getDate();
        LocalTime oldTime = existing.getTime();
        String oldTitle = existing.getTitle();
        String oldEventType = existing.getEventType();

        // Update fields
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getEventType() != null) existing.setEventType(updated.getEventType());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getDate() != null) existing.setDate(updated.getDate());
        if (updated.getTime() != null) existing.setTime(updated.getTime());

        CaseEventEntity savedEvent = caseEventRepository.save(existing);

        // Reset notified flag if date/time changed so reminder schedulers pick it up
        boolean dateOrTimeChanged = (updated.getDate() != null && !updated.getDate().equals(oldDate))
                || (updated.getTime() != null && !updated.getTime().equals(oldTime));
        if (dateOrTimeChanged) {
            savedEvent.setNotified(false);
            caseEventRepository.save(savedEvent);
        }

        // Send HEARING_RESCHEDULED notification to client
        try {
            CaseEntity caseEntity = savedEvent.getCaseEntity();
            Client client = caseEntity != null ? caseEntity.getClient() : null;
            if (client != null) {
                String formattedOldDate = oldDate != null ? oldDate.toString() : "TBD";
                String formattedOldTime = oldTime != null ? oldTime.toString() : "TBD";
                String formattedNewDate = savedEvent.getDate() != null ? savedEvent.getDate().toString() : "TBD";
                String formattedNewTime = savedEvent.getTime() != null ? savedEvent.getTime().toString() : "TBD";

                NotificationPayload payload = new NotificationPayload();
                payload.setType(NotificationType.HEARING_RESCHEDULED);
                payload.setRecipientName(client.getName());
                payload.setRecipientEmail(client.getEmail());
                payload.setRecipientPhone(client.getPhone());
                payload.setAdvocate(advocate);
                payload.setCaseEntity(caseEntity);
                payload.setClient(client);
                payload.setSubject(savedEvent.getEventType() + " Rescheduled — " + caseEntity.getCaseNumber());
                payload.setEmailBody(templateService.hearingRescheduledEmail(client.getName(),
                        caseEntity.getCaseNumber(), savedEvent.getEventType(),
                        oldDate, oldTime, savedEvent.getDate(), savedEvent.getTime(),
                        caseEntity.getCourtLevel(), "/cases/" + caseEntity.getId()));
                payload.setWhatsappMessage(templateService.hearingRescheduledWhatsApp(client.getName(),
                        caseEntity.getCaseNumber(),
                        oldDate, oldTime, savedEvent.getDate(), savedEvent.getTime(),
                        caseEntity.getCourtLevel()));
                payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_HEARING_RESCHEDULED);
                Map<String, String> templateParams = new HashMap<>();
                templateParams.put("1", client.getName());
                templateParams.put("2", caseEntity.getCaseNumber());
                templateParams.put("3", formattedNewDate);
                templateParams.put("4", formattedNewTime);
                payload.setWhatsappTemplateParameters(templateParams);
                notificationDispatcher.dispatchSafely(payload);
            }
        } catch (Exception e) {
            log.warn("Could not dispatch HEARING_RESCHEDULED notification: {}", e.getMessage());
        }

        // Record timeline
        try {
            CaseEntity caseForTimeline = savedEvent.getCaseEntity();
            String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String eventType = dateOrTimeChanged ? CaseTimelineService.HEARING_RESCHEDULED : CaseTimelineService.HEARING_UPDATED;
            String eventTitle = dateOrTimeChanged ? "Hearing Rescheduled" : "Hearing Updated";
            String dateStr = savedEvent.getDate() != null ? savedEvent.getDate().toString() : "";
            String timeStr = savedEvent.getTime() != null ? savedEvent.getTime().toString() : "";
            timelineService.recordEvent(
                    caseForTimeline != null ? caseForTimeline.getId() : null,
                    caseForTimeline != null && caseForTimeline.getClient() != null ? caseForTimeline.getClient().getId() : null,
                    advocate.getId(), eventType, eventTitle,
                    (savedEvent.getTitle() != null ? savedEvent.getTitle() + " — " : "") + dateStr + (timeStr.isEmpty() ? "" : " " + timeStr),
                    performedBy, "Hearing", savedEvent.getId()
            );
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String auditEventType = dateOrTimeChanged ? AuditLogService.HEARING_RESCHEDULED : AuditLogService.HEARING_UPDATED;
            String auditTitle = dateOrTimeChanged ? "Hearing Rescheduled" : "Hearing Updated";
            String auditDesc = (savedEvent.getTitle() != null ? savedEvent.getTitle() : "Hearing") +
                    (dateOrTimeChanged ? " rescheduled" : " updated");
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    auditEventType, AuditLogService.MODULE_HEARINGS,
                    auditTitle, auditDesc,
                    "Hearing", savedEvent.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        return savedEvent;
    }

    // ✅ Delete event (and optionally notify advocate)
    public void deleteEvent(String advocateEmail, Long eventId) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        CaseEventEntity event = caseEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized delete attempt.");
        }

        String eventTitle = event.getTitle();
        String eventTypeStr = event.getEventType();
        Long deletedEventId = event.getId();
        caseEventRepository.delete(event);

        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.HEARING_DELETED, AuditLogService.MODULE_HEARINGS,
                    "Hearing Deleted", (eventTitle != null ? eventTitle + " " : "") + eventTypeStr + " deleted",
                    "Hearing", deletedEventId, "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        // Optional: Notify advocate of deletion (in-app)
        String message = "Event deleted: " + eventTitle + " (" + eventTypeStr + ")";
        notificationService.createNotification(message, advocate);
    }

    // ✅ Helper: Auto-delete past events (optional)
    @Transactional
    public void autoCleanOldEvents() {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        caseEventRepository.deleteByDateBefore(cutoff);
    }
}
