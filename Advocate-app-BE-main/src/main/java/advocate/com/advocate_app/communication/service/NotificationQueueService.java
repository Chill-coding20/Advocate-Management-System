package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.entity.CommunicationSettings;
import advocate.com.advocate_app.communication.entity.NotificationQueue;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.repository.CommunicationSettingsRepository;
import advocate.com.advocate_app.communication.repository.NotificationQueueRepository;
import advocate.com.advocate_app.entity.Advocate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NotificationQueueService {

    private static final Logger log = LoggerFactory.getLogger(NotificationQueueService.class);

    private final NotificationQueueRepository queueRepository;
    private final CommunicationSettingsRepository settingsRepository;
    private final CommunicationDispatcher dispatcher;
    private final DuplicateProtectionService duplicateProtectionService;
    private final ObjectMapper objectMapper;

    public NotificationQueueService(NotificationQueueRepository queueRepository,
                                    CommunicationSettingsRepository settingsRepository,
                                    CommunicationDispatcher dispatcher,
                                    DuplicateProtectionService duplicateProtectionService,
                                    ObjectMapper objectMapper) {
        this.queueRepository = queueRepository;
        this.settingsRepository = settingsRepository;
        this.dispatcher = dispatcher;
        this.duplicateProtectionService = duplicateProtectionService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void enqueue(NotificationPayload payload, Advocate advocate) {
        if (advocate == null) {
            log.warn("Cannot enqueue: no advocate provided");
            return;
        }

        if (duplicateProtectionService.isDuplicate(payload, advocate)) {
            log.info("Duplicate suppressed for type={}", payload.getType());
            return;
        }

        CommunicationSettings settings = settingsRepository.findByAdvocate(advocate).orElse(null);
        boolean queueEnabled = settings != null && settings.isQueueEnabled();

        if (queueEnabled) {
            try {
                String payloadJson = objectMapper.writeValueAsString(payload);
                NotificationQueue queueItem = new NotificationQueue();
                queueItem.setStatus(NotificationStatus.PENDING);
                queueItem.setType(payload.getType());
                queueItem.setAdvocate(advocate);
                queueItem.setPayloadJson(payloadJson);
                queueItem.setMaxRetries(settings != null ? settings.getMaxRetryCount() : 4);
                queueItem.setCreatedAt(LocalDateTime.now());
                queueRepository.save(queueItem);
                log.info("Enqueued notification: type={} advocate={}", payload.getType(), advocate.getEmail());
            } catch (Exception e) {
                log.error("Failed to enqueue notification, falling back to direct dispatch: {}", e.getMessage());
                dispatcher.dispatchSafely(payload);
            }
        } else {
            dispatcher.dispatchSafely(payload);
        }
    }

    @Transactional
    public void processQueueItem(NotificationQueue queueItem) {
        try {
            queueItem.setStatus(NotificationStatus.PROCESSING);
            queueItem.setProcessingStartedAt(LocalDateTime.now());
            queueRepository.save(queueItem);

            NotificationPayload payload = objectMapper.readValue(queueItem.getPayloadJson(), NotificationPayload.class);
            payload.setAdvocate(queueItem.getAdvocate());

            dispatcher.dispatch(payload, queueItem.getAdvocate());

            queueRepository.delete(queueItem);
            log.info("Queue item processed: id={} type={}", queueItem.getId(), queueItem.getType());

        } catch (Exception e) {
            int retryCount = queueItem.getRetryCount() + 1;
            int maxRetries = queueItem.getMaxRetries();

            queueItem.setRetryCount(retryCount);
            queueItem.setLastError(e.getMessage() != null ? e.getMessage() : "Unknown error");

            if (retryCount >= maxRetries) {
                queueItem.setStatus(NotificationStatus.FAILED_PERMANENTLY);
                log.warn("Queue item failed permanently after {} retries: id={}", retryCount, queueItem.getId());
            } else {
                queueItem.setStatus(NotificationStatus.FAILED);
                long delayMs = getRetryDelay(retryCount);
                queueItem.setNextRetryAt(LocalDateTime.now().plusSeconds(delayMs / 1000));
                log.info("Queue item failed (retry {}/{}): id={} will retry at {}",
                        retryCount, maxRetries, queueItem.getId(), queueItem.getNextRetryAt());
            }
            queueRepository.save(queueItem);
        }
    }

    private long getRetryDelay(int attempt) {
        return switch (attempt) {
            case 1 -> 0;
            case 2 -> 120_000;
            case 3 -> 300_000;
            case 4 -> 900_000;
            default -> 900_000;
        };
    }
}
