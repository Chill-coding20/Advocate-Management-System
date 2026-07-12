package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.entity.NotificationQueue;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.repository.NotificationQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationQueueWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationQueueWorker.class);

    private final NotificationQueueRepository queueRepository;
    private final NotificationQueueService queueService;
    private final NotificationLogService logService;

    public NotificationQueueWorker(NotificationQueueRepository queueRepository,
                                   NotificationQueueService queueService,
                                   NotificationLogService logService) {
        this.queueRepository = queueRepository;
        this.queueService = queueService;
        this.logService = logService;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingQueue() {
        List<NotificationQueue> pendingItems = queueRepository.findReadyToProcess(
                NotificationStatus.PENDING, LocalDateTime.now());

        for (NotificationQueue item : pendingItems) {
            try {
                queueService.processQueueItem(item);
                logService.log(item.getAdvocate(), getRecipientFromPayload(item),
                        item.getType().name(), "EMAIL",
                        "INFO", "Queue item processed", null);
            } catch (Exception e) {
                log.error("Error processing queue item {}: {}", item.getId(), e.getMessage());
                logService.log(item.getAdvocate(), getRecipientFromPayload(item),
                        item.getType().name(), "EMAIL",
                        "ERROR", "Queue processing failed: " + e.getMessage(), null);
            }
        }
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retryFailedItems() {
        List<NotificationQueue> failedItems = queueRepository.findReadyToProcess(
                NotificationStatus.FAILED, LocalDateTime.now());

        for (NotificationQueue item : failedItems) {
            try {
                log.info("Retrying queue item: id={} attempt={}/{}",
                        item.getId(), item.getRetryCount() + 1, item.getMaxRetries());
                queueService.processQueueItem(item);
            } catch (Exception e) {
                log.error("Error retrying queue item {}: {}", item.getId(), e.getMessage());
            }
        }
    }

    private String getRecipientFromPayload(NotificationQueue item) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(item.getPayloadJson());
            if (root.has("recipientEmail") && !root.get("recipientEmail").isNull()) {
                return root.get("recipientEmail").asText();
            }
            if (root.has("recipientPhone") && !root.get("recipientPhone").isNull()) {
                return root.get("recipientPhone").asText();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
