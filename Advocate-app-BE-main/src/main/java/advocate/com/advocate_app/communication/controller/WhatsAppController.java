package advocate.com.advocate_app.communication.controller;

import advocate.com.advocate_app.communication.service.CommunicationDispatcher;
import advocate.com.advocate_app.communication.service.CommunicationHistoryService;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.communication.entity.NotificationHistory;
import advocate.com.advocate_app.communication.repository.CommunicationHistoryRepository;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.provider.whatsapp.WhatsAppProvider;
import advocate.com.advocate_app.communication.exception.MetaWhatsAppException;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.repository.AdvocateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppController.class);

    @Value("${whatsapp.verify.token:AdvocateApp2026SecureToken}")
    private String verifyToken;

    @Autowired
    private CommunicationDispatcher notificationDispatcher;

    @Autowired
    private CommunicationHistoryRepository historyRepository;

    @Autowired
    private CommunicationHistoryService historyService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private WhatsAppProvider whatsappProvider;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Webhook Verification (Meta calls this once to verify your endpoint)
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") String mode,
                                                @RequestParam("hub.verify_token") String token,
                                                @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("WhatsApp Webhook Verified!");
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * Webhook Event Receiver (Meta sends delivery status callbacks here)
     *
     * Handles:
     *  - messages (inbound messages from customers)
     *  - statuses (delivery receipts: sent, delivered, read, failed)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> body) {
        log.info("[WhatsAppWebhook] Received webhook payload: {}", body);

        try {
            String raw = objectMapper.writeValueAsString(body);
            JsonNode root = objectMapper.readTree(raw);

            // Handle delivery status callbacks
            JsonNode entryArray = root.get("entry");
            if (entryArray != null && entryArray.isArray()) {
                for (JsonNode entry : entryArray) {
                    JsonNode changes = entry.get("changes");
                    if (changes != null && changes.isArray()) {
                        for (JsonNode change : changes) {
                            JsonNode value = change.get("value");

                            // --- Status updates (delivery receipts) ---
                            JsonNode statuses = value != null ? value.get("statuses") : null;
                            if (statuses != null && statuses.isArray()) {
                                for (JsonNode status : statuses) {
                                    processStatusUpdate(status);
                                }
                            }

                            // --- Inbound messages (replies from customers) ---
                            JsonNode messages = value != null ? value.get("messages") : null;
                            if (messages != null && messages.isArray()) {
                                for (JsonNode msg : messages) {
                                    processInboundMessage(value, msg);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("[WhatsAppWebhook] Error processing webhook: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    /**
     * Processes a delivery status update from Meta.
     */
    private void processStatusUpdate(JsonNode status) {
        try {
            String wamId = status.has("id") ? status.get("id").asText() : null;
            String recipientId = status.has("recipient_id") ? status.get("recipient_id").asText() : null;
            String statusName = status.has("status") ? status.get("status").asText() : null;

            log.info("[WhatsAppWebhook] Status update: wamid={} recipient={} status={}",
                    wamId, recipientId, statusName);

            if (wamId == null) return;

            // Map Meta statuses to our history status
            if ("sent".equals(statusName) || "delivered".equals(statusName) || "read".equals(statusName)) {
                historyService.updateStatusByMessageId(wamId, statusName.toUpperCase());
            } else if ("failed".equals(statusName)) {
                JsonNode errors = status.get("errors");
                String errorMessage = null;
                Integer errorCode = null;
                if (errors != null && errors.isArray() && errors.size() > 0) {
                    JsonNode error = errors.get(0);
                    errorMessage = error.has("title") ? error.get("title").asText() : null;
                    errorCode = error.has("code") ? error.get("code").asInt() : null;
                    if (error.has("details")) {
                        errorMessage = error.get("details").asText();
                    }
                }
                log.warn("[WhatsAppWebhook] Delivery failed: wamid={} code={} message={}",
                        wamId, errorCode, errorMessage);
                historyService.updateStatusByMessageId(wamId, "FAILED", errorMessage, errorCode);
            }

        } catch (Exception e) {
            log.error("[WhatsAppWebhook] Error processing status update: {}", e.getMessage(), e);
        }
    }

    /**
     * Processes an inbound message from a customer.
     */
    private void processInboundMessage(JsonNode value, JsonNode msg) {
        try {
            String from = msg.has("from") ? msg.get("from").asText() : null;
            String text = msg.has("text") ? msg.get("text").get("body").asText() : null;
            log.info("[WhatsAppWebhook] Inbound message from {}: {}", from, text);
            // Future: could trigger auto-reply or store as conversation history
        } catch (Exception e) {
            log.error("[WhatsAppWebhook] Error processing inbound message: {}", e.getMessage(), e);
        }
    }

    /**
     * Send a manual WhatsApp message from the frontend
     */
    @PostMapping("/send-manual")
    public ResponseEntity<?> sendManualMessage(@RequestHeader("Authorization") String token,
                                               @RequestBody Map<String, String> request) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email).orElseThrow();

        String phone = request.get("phone");
        String message = request.get("message");
        String clientName = request.getOrDefault("clientName", "Client");

        NotificationPayload payload = new NotificationPayload();
        payload.setType(NotificationType.MANUAL_MESSAGE);
        payload.setRecipientName(clientName);
        payload.setRecipientPhone(phone);
        payload.setAdvocate(advocate);
        payload.setSubject("Manual WhatsApp Message");
        payload.setWhatsappMessage(message);

        notificationDispatcher.dispatchSafely(payload);
        return ResponseEntity.ok(Map.of("message", "Message dispatched successfully"));
    }

    /**
     * Resend a failed WhatsApp message
     * First tries text message. If Meta returns error 131047 (re-engagement),
     * falls back to a generic template message.
     */
    @PostMapping("/resend/{historyId}")
    public ResponseEntity<?> resendMessage(@RequestHeader("Authorization") String token,
                                           @PathVariable Long historyId) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email).orElseThrow();

        NotificationHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("History not found"));

        if (!history.getAdvocate().getId().equals(advocate.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        try {
            if ("WHATSAPP".equals(history.getChannel())) {
                String messageId;
                try {
                    messageId = whatsappProvider.sendMessage(history.getRecipientPhone(), history.getBody());
                } catch (MetaWhatsAppException e) {
                    // If error 131047 (re-engagement), fall back to template
                    if (e.getStatusCode() != null && isReEngagementError(e)) {
                        log.warn("[WhatsAppController] Text message failed (131047), retrying with template");
                        messageId = whatsappProvider.sendTemplateMessage(
                                history.getRecipientPhone(), EmailTemplateService.TEMPLATE_HELLO_WORLD, null);
                    } else {
                        throw e;
                    }
                }
                history.setStatus(NotificationStatus.SENT);
                history.setErrorMessage(null);
                history.setMetaMessageId(messageId);
                history.setStatusCode(null);
                history.setResponseBody(null);
            }
            historyRepository.save(history);
            return ResponseEntity.ok(Map.of("message", "Resent successfully"));
        } catch (MetaWhatsAppException e) {
            history.setErrorMessage(e.getMessage());
            history.setStatusCode(e.getStatusCode());
            historyRepository.save(history);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage(), "code", e.getStatusCode()));
        } catch (Exception e) {
            history.setErrorMessage(e.getMessage());
            historyRepository.save(history);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private boolean isReEngagementError(MetaWhatsAppException e) {
        try {
            JsonNode root = objectMapper.readTree(e.getResponseBody());
            JsonNode error = root.get("error");
            if (error != null && error.has("code")) {
                return error.get("code").asInt() == 131047;
            }
            JsonNode errorData = root.get("error_data");
            if (errorData != null && errorData.has("code")) {
                return errorData.get("code").asInt() == 131047;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
