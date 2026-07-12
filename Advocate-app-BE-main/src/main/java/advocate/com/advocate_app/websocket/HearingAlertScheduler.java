package advocate.com.advocate_app.websocket;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEventEntity;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.CaseEventRepository;
import advocate.com.advocate_app.websocket.dto.HearingAlertEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HearingAlertScheduler {

    @Autowired
    private CaseEventRepository caseEventRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Scheduled(fixedRate = 60000)
    public void checkUpcomingHearings() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime windowStart = now;
        LocalTime windowEnd = now.plusHours(1);

        List<CaseEventEntity> upcoming = caseEventRepository.findEventsForTodayAndTomorrow(today, today);
        if (upcoming.isEmpty()) return;

        for (CaseEventEntity event : upcoming) {
            if (event.getTime() == null) continue;
            if (event.getTime().isAfter(windowStart) && event.getTime().isBefore(windowEnd)) {
                Advocate advocate = event.getAdvocate();
                if (advocate == null) continue;
                String caseNumber = event.getCaseEntity() != null ? event.getCaseEntity().getCaseNumber() : "N/A";
                HearingAlertEvent alert = new HearingAlertEvent(
                        event.getId(),
                        event.getTitle(),
                        caseNumber,
                        today.toString(),
                        event.getTime().toString(),
                        "Hearing \"" + event.getTitle() + "\" is coming up at " + event.getTime()
                );
                webSocketService.sendHearingAlert(advocate.getEmail(), alert);
            }
        }
    }
}
