package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.entity.NotificationHistory;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.repository.CommunicationHistoryRepository;
import advocate.com.advocate_app.entity.Advocate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DuplicateProtectionService {

    private static final Logger log = LoggerFactory.getLogger(DuplicateProtectionService.class);
    private static final long DUP_WINDOW_MINUTES = 5;

    private final CommunicationHistoryRepository historyRepository;

    public DuplicateProtectionService(CommunicationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public boolean isDuplicate(NotificationPayload payload, Advocate advocate) {
        if (payload == null || advocate == null) return false;

        LocalDateTime since = LocalDateTime.now().minusMinutes(DUP_WINDOW_MINUTES);
        List<NotificationHistory> recent = historyRepository
                .findByAdvocateAndCreatedAtAfter(advocate, since);

        for (NotificationHistory h : recent) {
            if (h.getType() == payload.getType()
                    && h.getStatus() == NotificationStatus.SENT
                    && recipientsMatch(h, payload)
                    && entitiesMatch(h, payload)) {
                log.info("Duplicate notification suppressed: type={} recipient={}", payload.getType(), getRecipient(payload));
                return true;
            }
        }
        return false;
    }

    private boolean recipientsMatch(NotificationHistory h, NotificationPayload p) {
        String hRec = h.getRecipientEmail() != null ? h.getRecipientEmail() : h.getRecipientPhone();
        String pRec = p.getRecipientEmail() != null ? p.getRecipientEmail() : p.getRecipientPhone();
        return hRec != null && hRec.equals(pRec);
    }

    private boolean entitiesMatch(NotificationHistory h, NotificationPayload p) {
        Long hCaseId = h.getCaseEntity() != null ? h.getCaseEntity().getId() : null;
        Long pCaseId = p.getCaseEntity() != null ? p.getCaseEntity().getId() : null;
        if (hCaseId != null && pCaseId != null && hCaseId.equals(pCaseId)) return true;
        Long hClientId = h.getClient() != null ? h.getClient().getId() : null;
        Long pClientId = p.getClient() != null ? p.getClient().getId() : null;
        if (hClientId != null && pClientId != null && hClientId.equals(pClientId)) return true;
        return false;
    }

    private String getRecipient(NotificationPayload p) {
        return p.getRecipientEmail() != null ? p.getRecipientEmail() : p.getRecipientPhone();
    }
}
