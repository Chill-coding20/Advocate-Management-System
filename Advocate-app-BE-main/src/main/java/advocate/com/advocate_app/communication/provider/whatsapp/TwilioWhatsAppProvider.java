package advocate.com.advocate_app.communication.provider.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Twilio WhatsApp API provider.
 *
 * To activate: set WHATSAPP_PROVIDER=twilio in environment variables.
 * Also set:
 *   TWILIO_ACCOUNT_SID=your-account-sid
 *   TWILIO_AUTH_TOKEN=your-auth-token
 *   TWILIO_WHATSAPP_FROM=whatsapp:+14155238886
 *
 * Docs: https://www.twilio.com/docs/whatsapp/api
 *
 * NOTE: The actual HTTP call is stubbed. Add Twilio SDK or RestTemplate to implement.
 */
@Component
@ConditionalOnProperty(name = "app.notification.whatsapp.provider", havingValue = "twilio")
public class TwilioWhatsAppProvider implements WhatsAppProvider {

    private static final Logger log = LoggerFactory.getLogger(TwilioWhatsAppProvider.class);

    @Value("${app.notification.whatsapp.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.notification.whatsapp.twilio.auth-token:}")
    private String authToken;

    @Value("${app.notification.whatsapp.twilio.from:}")
    private String fromNumber;

    private static final String TWILIO_API_URL = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";

    @Override
    public String sendMessage(String phone, String message) {
        if (accountSid == null || accountSid.isBlank() ||
            authToken == null || authToken.isBlank()) {
            log.error("[TwilioWhatsApp] Missing credentials. Set TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN.");
            return null;
        }

        String url = String.format(TWILIO_API_URL, accountSid);
        String toNumber = "whatsapp:" + phone;

        log.info("[TwilioWhatsApp][STUB] Would POST to: {}", url);
        log.info("[TwilioWhatsApp][STUB] From: {} | To: {}", fromNumber, toNumber);
        log.info("[TwilioWhatsApp][STUB] Message: {}", message);
        return "twilio_stub_id_" + System.currentTimeMillis();
    }

    @Override
    public String sendTemplateMessage(String phone, String templateName, Map<String, String> parameters) {
        log.info("[TwilioWhatsApp][STUB] Template request to: {}", phone);
        log.info("[TwilioWhatsApp][STUB] Template: {}", templateName);
        log.info("[TwilioWhatsApp][STUB] Parameters: {}", parameters);
        return "twilio_stub_template_id_" + System.currentTimeMillis();
    }

    @Override
    public String getProviderName() {
        return "TWILIO";
    }
}
