package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.provider.NotificationProvider;
import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.entity.CommunicationSettings;
import advocate.com.advocate_app.communication.entity.NotificationHistory;
import advocate.com.advocate_app.communication.entity.NotificationQueue;
import advocate.com.advocate_app.communication.repository.CommunicationHistoryRepository;
import advocate.com.advocate_app.communication.repository.CommunicationSettingsRepository;
import advocate.com.advocate_app.communication.repository.NotificationQueueRepository;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunicationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CommunicationDispatcher.class);

    private final List<NotificationProvider> providers;
    private final CommunicationHistoryRepository historyRepository;
    private final CommunicationSettingsRepository settingsRepository;
    private final NotificationQueueRepository queueRepository;
    private final DuplicateProtectionService duplicateProtectionService;
    private final NotificationLogService logService;
    private final ObjectMapper objectMapper;

    @Autowired
    private AuditLogService auditLogService;

    public CommunicationDispatcher(List<NotificationProvider> providers,
                                   CommunicationHistoryRepository historyRepository,
                                   CommunicationSettingsRepository settingsRepository,
                                   NotificationQueueRepository queueRepository,
                                   DuplicateProtectionService duplicateProtectionService,
                                   NotificationLogService logService,
                                   ObjectMapper objectMapper) {
        this.providers = providers;
        this.historyRepository = historyRepository;
        this.settingsRepository = settingsRepository;
        this.queueRepository = queueRepository;
        this.duplicateProtectionService = duplicateProtectionService;
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    /**
     * Dispatch a notification. If queue is enabled for the advocate, enqueues it.
     * If queue is disabled, sends immediately (synchronous).
     */
    public void dispatchSafely(NotificationPayload payload) {
        Advocate advocate = payload.getAdvocate();
        if (advocate == null) {
            log.warn("Cannot dispatch notification: no advocate in payload");
            return;
        }

        if (duplicateProtectionService.isDuplicate(payload, advocate)) {
            log.info("Duplicate suppressed: type={}", payload.getType());
            return;
        }

        CommunicationSettings settings = settingsRepository.findByAdvocate(advocate).orElse(null);
        boolean queueEnabled = settings != null && settings.isQueueEnabled();

        if (queueEnabled) {
            enqueueNotification(payload, advocate, settings);
        } else {
            try {
                dispatch(payload, advocate);
            } catch (Exception e) {
                log.error("Error dispatching notification: {}", e.getMessage(), e);
            }
        }
    }

    private void enqueueNotification(NotificationPayload payload, Advocate advocate, CommunicationSettings settings) {
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
            logService.log(advocate, getRecipient(payload), payload.getType().name(),
                    "QUEUE", "INFO", "Notification enqueued", null);
        } catch (Exception e) {
            log.error("Failed to enqueue, falling back to direct dispatch: {}", e.getMessage());
            try {
                dispatch(payload, advocate);
            } catch (Exception ex) {
                log.error("Fallback dispatch also failed: {}", ex.getMessage());
            }
        }
    }

    /**
     * Synchronous dispatch - sends immediately via providers. Used by test endpoint and queue worker.
     */
    public NotificationResult dispatch(NotificationPayload payload, Advocate advocate) {
        List<NotificationProvider> eligible = findEligibleProviders(payload);

        if (eligible.isEmpty()) {
            log.warn("No eligible provider found for type={}", payload.getType());
            saveHistory(payload, advocate, payload.getChannel(), NotificationStatus.FAILED,
                    null, "No eligible provider for payload", null);
            return new NotificationResult(false, null,
                    "No eligible provider for payload (channel=" + payload.getChannel() + ")");
        }

        boolean anySuccess = false;
        String lastResponse = null;
        String lastError = null;

        for (NotificationProvider provider : eligible) {
            NotificationChannel providerChannel = resolveChannel(provider);
            try {
                NotificationProvider.NotificationResult result = provider.send(payload);
                if (result.isSuccess()) {
                    saveHistory(payload, advocate, providerChannel, NotificationStatus.SENT,
                            result.getProviderResponse(), null, null);
                    anySuccess = true;
                    lastResponse = result.getProviderResponse();
                    logService.log(advocate, getRecipient(payload), payload.getType().name(),
                            providerChannel.name(), "INFO", "Sent successfully", result.getProviderResponse());
                } else {
                    String error = result.getErrorMessage();
                    saveHistory(payload, advocate, providerChannel, NotificationStatus.FAILED,
                            result.getProviderResponse(), error, null);
                    lastError = error;
                    logService.log(advocate, getRecipient(payload), payload.getType().name(),
                            providerChannel.name(), "ERROR", "Send failed: " + error, null);
                }
            } catch (Exception e) {
                log.warn("Provider {} threw: {}", provider.getClass().getSimpleName(), e.getMessage());
                String stackTrace = getStackTrace(e);
                saveHistory(payload, advocate, providerChannel, NotificationStatus.FAILED,
                        null, e.getMessage(), stackTrace);
                lastError = e.getMessage();
                logService.log(advocate, getRecipient(payload), payload.getType().name(),
                        providerChannel.name(), "ERROR", "Provider exception: " + e.getMessage(), stackTrace);
            }
        }

        if (anySuccess) {
            // Record audit log for successful sends
            try {
                String actionType = (payload.getEmailBody() != null || payload.getRecipientEmail() != null)
                        ? AuditLogService.EMAIL_SENT : AuditLogService.WHATSAPP_SENT;
                String module = (payload.getEmailBody() != null || payload.getRecipientEmail() != null)
                        ? AuditLogService.MODULE_COMMUNICATION : AuditLogService.MODULE_COMMUNICATION;
                String description = (payload.getEmailBody() != null || payload.getRecipientEmail() != null)
                        ? "Email sent to " + getRecipient(payload) : "WhatsApp sent to " + getRecipient(payload);
                auditLogService.recordAction(
                        advocate.getId(),
                        advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                        actionType, module,
                        payload.getType().name().replace("_", " "),
                        description,
                        "Communication", null, "SUCCESS"
                );
            } catch (Exception e) {
                log.warn("Could not record audit log: {}", e.getMessage());
            }
            return new NotificationResult(true, lastResponse, null);
        }
        return new NotificationResult(false, null, lastError);
    }

    private NotificationChannel resolveChannel(NotificationProvider provider) {
        if (provider.supports(NotificationChannel.EMAIL)) return NotificationChannel.EMAIL;
        if (provider.supports(NotificationChannel.WHATSAPP)) return NotificationChannel.WHATSAPP;
        return null;
    }

    private List<NotificationProvider> findEligibleProviders(NotificationPayload payload) {
        if (payload.getChannel() != null) {
            return providers.stream()
                    .filter(p -> p.supports(payload.getChannel()))
                    .collect(Collectors.toList());
        }
        List<NotificationProvider> eligible = new ArrayList<>();
        for (NotificationProvider provider : providers) {
            if (provider.supports(NotificationChannel.EMAIL) && hasEmailContent(payload)) {
                eligible.add(provider);
            } else if (provider.supports(NotificationChannel.WHATSAPP) && hasWhatsAppContent(payload)) {
                eligible.add(provider);
            }
        }
        return eligible;
    }

    private boolean hasEmailContent(NotificationPayload payload) {
        return payload.getEmailBody() != null || payload.getRecipientEmail() != null;
    }

    private boolean hasWhatsAppContent(NotificationPayload payload) {
        return payload.getWhatsappMessage() != null || payload.getRecipientPhone() != null;
    }

    public void saveHistory(NotificationPayload payload, Advocate advocate,
                            NotificationChannel channel, NotificationStatus status,
                            String providerResponse, String errorMessage, String failureReason) {
        try {
            NotificationHistory history = new NotificationHistory();
            history.setAdvocate(advocate);
            history.setChannel(channel);
            history.setType(payload.getType());
            history.setRecipient(getRecipient(payload));
            history.setRecipientName(payload.getRecipientName());
            history.setRecipientEmail(payload.getRecipientEmail());
            history.setRecipientPhone(payload.getRecipientPhone());
            history.setSubject(payload.getSubject());
            history.setMessage(payload.getMessage());
            history.setBody(payload.getEmailBody() != null ? payload.getEmailBody() : payload.getWhatsappMessage());
            history.setStatus(status);
            history.setProviderResponse(providerResponse);
            history.setErrorMessage(errorMessage);
            history.setFailureReason(failureReason);
            history.setRetryCount(0);
            history.setTriggeredBy(payload.getType().name());

            if (payload.getCaseEntity() != null) {
                history.setCaseEntity(payload.getCaseEntity());
                history.setEntity("Case");
                history.setEntityId(payload.getCaseEntity().getId());
            }
            if (payload.getClient() != null) {
                history.setClient(payload.getClient());
                if (history.getEntity() == null) {
                    history.setEntity("Client");
                    history.setEntityId(payload.getClient().getId());
                }
            }

            if (status == NotificationStatus.SENT) {
                history.setSentAt(LocalDateTime.now());
            } else if (status == NotificationStatus.FAILED || status == NotificationStatus.FAILED_PERMANENTLY) {
                history.setSentAt(LocalDateTime.now());
                history.setFailedAt(LocalDateTime.now());
            } else {
                history.setSentAt(LocalDateTime.now());
            }

            historyRepository.save(history);
        } catch (Exception e) {
            log.error("Failed to save notification history: {}", e.getMessage());
        }
    }

    private String getRecipient(NotificationPayload payload) {
        if (payload.getRecipientEmail() != null) return payload.getRecipientEmail();
        if (payload.getRecipientPhone() != null) return payload.getRecipientPhone();
        return "unknown";
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement el : e.getStackTrace()) {
            sb.append("  at ").append(el.toString()).append("\n");
            if (sb.length() > 2000) break;
        }
        return sb.toString();
    }

    public static class NotificationResult {
        private final boolean success;
        private final String providerResponse;
        private final String errorMessage;

        public NotificationResult(boolean success, String providerResponse, String errorMessage) {
            this.success = success;
            this.providerResponse = providerResponse;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getProviderResponse() { return providerResponse; }
        public String getErrorMessage() { return errorMessage; }
    }
}
