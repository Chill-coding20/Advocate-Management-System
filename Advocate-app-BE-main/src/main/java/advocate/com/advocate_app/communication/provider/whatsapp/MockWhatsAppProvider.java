package advocate.com.advocate_app.communication.provider.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mock WhatsApp provider — logs messages to console instead of sending real ones.
 * Active when: app.notification.whatsapp.provider=mock (default)
 *
 * This is the safe default for development and testing.
 */
@Component
@ConditionalOnProperty(name = "app.notification.whatsapp.provider", havingValue = "mock", matchIfMissing = true)
public class MockWhatsAppProvider implements WhatsAppProvider {

    private static final Logger log = LoggerFactory.getLogger(MockWhatsAppProvider.class);

    @Override
    public String sendMessage(String phone, String message) {
        log.info("=================================================");
        log.info("[MOCK WHATSAPP] To: {}", phone != null ? phone : "N/A");
        log.info("[MOCK WHATSAPP] Message: {}", message);
        log.info("=================================================");
        return "mock_msg_id_" + System.currentTimeMillis();
    }

    @Override
    public String sendTemplateMessage(String phone, String templateName, Map<String, String> parameters) {
        log.info("=================================================");
        log.info("[MOCK WHATSAPP TEMPLATE] To: {}", phone);
        log.info("[MOCK WHATSAPP TEMPLATE] Template: {}", templateName);
        log.info("[MOCK WHATSAPP TEMPLATE] Parameters: {}", parameters);
        log.info("=================================================");
        return "mock_template_msg_id_" + System.currentTimeMillis();
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }
}
