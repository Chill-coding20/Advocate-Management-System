package advocate.com.advocate_app.communication.provider;

import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.entity.CommunicationSettings;
import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.repository.CommunicationSettingsRepository;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
public class WhatsAppNotificationProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationProvider.class);

    private static final String MESSAGES_PATH = "/%s/messages";

    private final CommunicationSettingsRepository settingsRepository;
    private final AdvocateRepository advocateRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WhatsAppNotificationProvider(CommunicationSettingsRepository settingsRepository,
                                         AdvocateRepository advocateRepository,
                                         RestTemplate restTemplate,
                                         ObjectMapper objectMapper) {
        this.settingsRepository = settingsRepository;
        this.advocateRepository = advocateRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.WHATSAPP;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        Instant start = Instant.now();

        Advocate advocate = lookupAdvocate(payload);
        if (advocate == null) {
            log.warn("Advocate not found for WhatsApp");
            return new NotificationResult(false, null, "Advocate not found");
        }

        CommunicationSettings settings = settingsRepository.findByAdvocate(advocate).orElse(null);
        if (settings == null || !settings.isWhatsappEnabled()) {
            log.warn("WhatsApp is disabled for advocate: {}", advocate.getEmail());
            return new NotificationResult(false, null, "WhatsApp is disabled");
        }

        String phoneNumberId = resolvePhoneNumberId(settings);
        String accessToken = resolveAccessToken(settings);
        String apiBaseUrl = resolveApiBaseUrl(settings);

        if (phoneNumberId == null || accessToken == null) {
            log.warn("WhatsApp credentials not configured for advocate: {}", advocate.getEmail());
            return new NotificationResult(false, null, "WhatsApp credentials not configured");
        }

        String recipientPhone = payload.getRecipientPhone();
        if (recipientPhone == null || recipientPhone.isBlank()) {
            log.warn("No recipient phone provided");
            return new NotificationResult(false, null, "Recipient phone is required");
        }

        String normalizedPhone;
        try {
            normalizedPhone = normalizePhone(recipientPhone);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid phone number: {} — {}", recipientPhone, e.getMessage());
            return new NotificationResult(false, null, e.getMessage());
        }

        String messageBody = payload.getWhatsappMessage();
        if (messageBody == null || messageBody.isBlank()) {
            messageBody = payload.getMessage();
        }
        if (messageBody == null || messageBody.isBlank()) {
            log.warn("No message content for WhatsApp");
            return new NotificationResult(false, null, "Message content is required");
        }

        messageBody = resolvePlaceholders(messageBody, payload.getVariables());

        String endpoint = buildEndpoint(apiBaseUrl, phoneNumberId);

        try {
            String requestJson = buildTextRequest(normalizedPhone, messageBody);
            log.info("[WhatsApp] Sending text message to {} | advocate={}", normalizedPhone, advocate.getEmail());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    endpoint,
                    new HttpEntity<>(requestJson, headers),
                    String.class);

            String responseBody = response.getBody();
            int httpStatus = response.getStatusCode().value();
            long elapsed = Duration.between(start, Instant.now()).toMillis();

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("[WhatsApp] API returned non-2xx status={} body={}", httpStatus, responseBody);
                return new NotificationResult(false, responseBody,
                        "WhatsApp API returned HTTP " + httpStatus);
            }

            String messageId = extractMessageId(responseBody);
            log.info("[WhatsApp] Sent successfully | to={} wamid={} duration={}ms",
                    normalizedPhone, messageId, elapsed);

            String providerResponse = "wamid=" + messageId + " | status=" + httpStatus;
            return new NotificationResult(true, providerResponse, null);

        } catch (HttpStatusCodeException e) {
            int statusCode = e.getStatusCode().value();
            String errorBody = e.getResponseBodyAsString();
            long elapsed = Duration.between(start, Instant.now()).toMillis();

            String errorMessage = formatMetaError(statusCode, errorBody);
            log.warn("[WhatsApp] HTTP {} | to={} | duration={}ms | error={}",
                    statusCode, normalizedPhone, elapsed, errorMessage);

            return new NotificationResult(false, errorBody, errorMessage);

        } catch (Exception e) {
            long elapsed = Duration.between(start, Instant.now()).toMillis();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown WhatsApp error";
            log.warn("[WhatsApp] Failed | to={} | duration={}ms | error={}",
                    normalizedPhone, elapsed, errorMsg);
            return new NotificationResult(false, null, errorMsg);
        }
    }

    private Advocate lookupAdvocate(NotificationPayload payload) {
        if (payload.getAdvocate() != null) {
            return payload.getAdvocate();
        }
        String email = payload.getAdvocateEmail();
        if (email != null && !email.isBlank()) {
            return advocateRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    private String resolvePhoneNumberId(CommunicationSettings s) {
        if (s.getWhatsappPhoneNumberId() != null && !s.getWhatsappPhoneNumberId().isBlank()) {
            return s.getWhatsappPhoneNumberId();
        }
        return null;
    }

    private String resolveAccessToken(CommunicationSettings s) {
        if (s.getWhatsappAccessToken() != null && !s.getWhatsappAccessToken().isBlank()) {
            return s.getWhatsappAccessToken();
        }
        return null;
    }

    private String resolveApiBaseUrl(CommunicationSettings s) {
        return "https://graph.facebook.com/v23.0";
    }

    String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() == 10) {
            return "91" + digits;
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits;
        }
        if (digits.length() == 11 && digits.startsWith("1")) {
            return digits;
        }
        if (digits.length() > 12) {
            digits = digits.substring(digits.length() - 12);
            if (digits.startsWith("91")) {
                return digits;
            }
        }

        throw new IllegalArgumentException("Invalid phone number format: " + phone
                + " — expected 10 digits (9876543210), 12 digits with country code (919876543210), or +91 format");
    }

    private String buildEndpoint(String apiBaseUrl, String phoneNumberId) {
        String base = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        return base + String.format(MESSAGES_PATH, phoneNumberId);
    }

    private String buildTextRequest(String to, String body) throws JsonProcessingException {
        TextPayload textPayload = new TextPayload(to, body);
        return objectMapper.writeValueAsString(textPayload);
    }

    private String extractMessageId(String responseBody) {
        if (responseBody == null) return null;
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messages = root.get("messages");
            if (messages != null && messages.isArray() && messages.size() > 0) {
                JsonNode idNode = messages.get(0).get("id");
                return idNode != null ? idNode.asText() : null;
            }
        } catch (Exception e) {
            log.warn("[WhatsApp] Could not extract message ID from response: {}", responseBody);
        }
        return null;
    }

    private String formatMetaError(int statusCode, String errorBody) {
        String summary = "WhatsApp API failed (HTTP " + statusCode + ")";
        if (errorBody == null || errorBody.isBlank()) return summary;

        try {
            JsonNode root = objectMapper.readTree(errorBody);
            JsonNode error = root.get("error");
            if (error != null) {
                String message = error.has("message") ? error.get("message").asText() : null;
                Integer code = error.has("code") ? error.get("code").asInt() : null;
                Integer errorSubcode = error.has("error_subcode") ? error.get("error_subcode").asInt() : null;
                String type = error.has("type") ? error.get("type").asText() : null;

                StringBuilder sb = new StringBuilder(summary);
                if (code != null) sb.append(" | code=").append(code);
                if (errorSubcode != null) sb.append(" subcode=").append(errorSubcode);
                if (type != null) sb.append(" type=").append(type);
                if (message != null) sb.append(" — ").append(message);
                return sb.toString();
            }
            JsonNode errorData = root.get("error_data");
            if (errorData != null) {
                String details = errorData.has("details") ? errorData.get("details").asText() : null;
                if (details != null) return summary + " — " + details;
            }
        } catch (Exception ignored) {}

        return summary + " — " + errorBody;
    }

    private String resolvePlaceholders(String text, Map<String, String> variables) {
        if (text == null) return "";
        if (variables == null) return text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            text = text.replace(key, value);
        }
        return text;
    }

    // ======================== TEXT MESSAGE DTO ========================

    @JsonPropertyOrder({"messaging_product", "to", "type", "text"})
    private static class TextPayload {
        @JsonProperty("messaging_product")
        private final String messagingProduct = "whatsapp";
        private final String to;
        private final String type = "text";
        private final TextBody text;

        TextPayload(String to, String body) {
            this.to = to;
            this.text = new TextBody(body);
        }

        public String getMessagingProduct() { return messagingProduct; }
        public String getTo() { return to; }
        public String getType() { return type; }
        public TextBody getText() { return text; }
    }

    private static class TextBody {
        @JsonProperty("preview_url")
        private final boolean previewUrl = false;
        private final String body;

        TextBody(String body) { this.body = body; }
        public boolean isPreviewUrl() { return previewUrl; }
        public String getBody() { return body; }
    }
}
