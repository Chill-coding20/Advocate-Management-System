package advocate.com.advocate_app.communication.provider.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import advocate.com.advocate_app.communication.exception.MetaWhatsAppException;

/**
 * Meta Business Cloud API WhatsApp provider.
 *
 * To activate: set app.notification.whatsapp.provider=meta.
 * Also configure:
 *   app.notification.whatsapp.meta.phone-number-id
 *   app.notification.whatsapp.meta.access-token
 *
 * Docs: https://developers.facebook.com/docs/whatsapp/cloud-api/messages
 */
@Component
@ConditionalOnProperty(name = "app.notification.whatsapp.provider", havingValue = "meta")
public class MetaWhatsAppProvider implements WhatsAppProvider {

    private static final Logger log = LoggerFactory.getLogger(MetaWhatsAppProvider.class);

    @Value("${app.notification.whatsapp.meta.phone-number-id}")
    private String phoneNumberId;

    @Value("${app.notification.whatsapp.meta.access-token}")
    private String accessToken;

    @Value("${whatsapp.api.url:https://graph.facebook.com/v23.0}")
    private String apiBaseUrl;

    @Value("${app.notification.whatsapp.meta.default-language:en_US}")
    private String defaultLanguage;

    private String currentTemplateName;
    private String currentLanguageCode;

    private static final String MESSAGES_PATH = "/%s/messages";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MetaWhatsAppProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String sendMessage(String phone, String message) {
        assertCredentialsConfigured();

        String recipient = normalizeIndianPhoneNumber(phone);
        String endpoint = buildEndpoint();
        WhatsAppTextRequest request = new WhatsAppTextRequest(recipient, message);
        String payload = toJson(request);

        log.info("[MetaWhatsApp] Sending text message. endpoint={} recipient={} payload={}",
                endpoint, recipient, payload);

        return doHttpPost(endpoint, payload, recipient);
    }

    @Override
    public String sendTemplateMessage(String phone, String templateName, Map<String, String> parameters) {
        assertCredentialsConfigured();

        String recipient = normalizeIndianPhoneNumber(phone);
        String endpoint = buildEndpoint();

        this.currentTemplateName = templateName;
        this.currentLanguageCode = defaultLanguage;

        WhatsAppTemplateRequest request = new WhatsAppTemplateRequest(recipient, templateName, parameters, defaultLanguage);
        String payload = toJson(request);

        log.info("[MetaWhatsApp] Sending template message. endpoint={} recipient={} template={} language={} payload={}",
                endpoint, recipient, templateName, defaultLanguage, payload);

        try {
            return doHttpPost(endpoint, payload, recipient);
        } catch (MetaWhatsAppException e) {
            log.error("[MetaWhatsApp] Template '{}' with language '{}' failed for recipient {} | Meta error: {}",
                    templateName, defaultLanguage, recipient, e.getResponseBody());
            throw e;
        } finally {
            this.currentTemplateName = null;
            this.currentLanguageCode = null;
        }
    }

    @Override
    public String getProviderName() {
        return "META";
    }

    private String doHttpPost(String endpoint, String payload, String recipient) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    endpoint,
                    new HttpEntity<>(payload, headers),
                    String.class);

            String responseBody = response.getBody();
            log.info("[MetaWhatsApp] Response received. status={} body={}",
                    response.getStatusCode().value(), responseBody);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MetaWhatsAppException(
                        response.getStatusCode().value(),
                        "WhatsApp API failed with status: " + response.getStatusCode(),
                        responseBody);
            }

            String messageId = extractMessageId(responseBody);
            log.info("[MetaWhatsApp] Successfully sent message to {} | wamid={}", recipient, messageId);
            return messageId;

        } catch (HttpStatusCodeException e) {
            String errorBody = e.getResponseBodyAsString();
            if (currentTemplateName != null) {
                log.error("[MetaWhatsApp] Template error. template={} language={} status={} body={}",
                        currentTemplateName, currentLanguageCode, e.getStatusCode().value(), errorBody);
            } else {
                log.error("[MetaWhatsApp] Meta returned error. status={} body={}",
                        e.getStatusCode().value(), errorBody);
            }
            throw new MetaWhatsAppException(e.getStatusCode().value(), e.getMessage(), errorBody, e);
        } catch (MetaWhatsAppException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("[MetaWhatsApp] Error sending message to {}: {}", recipient, e.getMessage(), e);
            throw e;
        }
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
            log.warn("[MetaWhatsApp] Could not extract message ID from response: {}", responseBody);
        }
        return null;
    }

    private void assertCredentialsConfigured() {
        if (!StringUtils.hasText(phoneNumberId) || !StringUtils.hasText(accessToken)) {
            throw new RuntimeException("[MetaWhatsApp] Missing credentials. Set app.notification.whatsapp.meta.phone-number-id and app.notification.whatsapp.meta.access-token in application.properties.");
        }
    }

    private String buildEndpoint() {
        return "%s%s".formatted(trimTrailingSlash(apiBaseUrl), String.format(MESSAGES_PATH, phoneNumberId));
    }

    private String normalizeIndianPhoneNumber(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("[MetaWhatsApp] Recipient phone number is required.");
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "91" + digits;
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits;
        }

        throw new IllegalArgumentException("[MetaWhatsApp] Indian phone number must be 10 digits or 12 digits starting with 91.");
    }

    private String toJson(Object request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("[MetaWhatsApp] Unable to serialize WhatsApp payload.", e);
        }
    }

    private String maskToken(String token) {
        if (!StringUtils.hasText(token) || token.length() <= 8) {
            return "********";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    // ======================== TEXT MESSAGE DTO ========================

    @JsonPropertyOrder({"messaging_product", "to", "type", "text"})
    private static class WhatsAppTextRequest {
        @JsonProperty("messaging_product")
        private final String messagingProduct = "whatsapp";
        private final String to;
        private final String type = "text";
        private final Text text;

        WhatsAppTextRequest(String to, String body) {
            this.to = to;
            this.text = new Text(body);
        }

        public String getMessagingProduct() { return messagingProduct; }
        public String getTo() { return to; }
        public String getType() { return type; }
        public Text getText() { return text; }
    }

    private static class Text {
        private final String body;
        Text(String body) { this.body = body; }
        public String getBody() { return body; }
    }

    // ======================== TEMPLATE MESSAGE DTO ========================

    @JsonPropertyOrder({"messaging_product", "to", "type", "template"})
    private static class WhatsAppTemplateRequest {
        @JsonProperty("messaging_product")
        private final String messagingProduct = "whatsapp";
        private final String to;
        private final String type = "template";
        private final Template template;

        WhatsAppTemplateRequest(String to, String templateName, Map<String, String> parameters, String languageCode) {
            this.to = to;
            this.template = new Template(templateName, parameters, languageCode);
        }

        public String getMessagingProduct() { return messagingProduct; }
        public String getTo() { return to; }
        public String getType() { return type; }
        public Template getTemplate() { return template; }
    }

    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private static class Template {
        private final String name;
        private final Language language;
        private List<TemplateComponent> components;

        Template(String name, Map<String, String> parameters, String languageCode) {
            this.name = name;
            this.language = new Language(languageCode);
            if (!"hello_world".equals(name) && parameters != null && !parameters.isEmpty()) {
                this.components = new ArrayList<>();
                List<Parameter> paramList = new ArrayList<>();
                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    if (entry.getValue() != null) {
                        paramList.add(new Parameter("text", entry.getValue()));
                    }
                }
                if (!paramList.isEmpty()) {
                    this.components.add(new TemplateComponent("body", paramList));
                }
            }
        }

        public String getName() { return name; }
        public Language getLanguage() { return language; }
        public List<TemplateComponent> getComponents() { return components; }
    }

    private static class Language {
        private final String code;
        Language(String code) { this.code = code; }
        public String getCode() { return code; }
    }

    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private static class TemplateComponent {
        private final String type;
        private List<Parameter> parameters;

        TemplateComponent(String type, List<Parameter> parameters) {
            this.type = type;
            if (parameters != null && !parameters.isEmpty()) {
                this.parameters = parameters;
            }
        }

        public String getType() { return type; }
        public List<Parameter> getParameters() { return parameters; }
    }

    private static class Parameter {
        private final String type;
        private final String text;

        Parameter(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() { return type; }
        public String getText() { return text; }
    }
}
