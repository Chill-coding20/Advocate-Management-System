package advocate.com.advocate_app.communication.provider.whatsapp;

import java.util.Map;

/**
 * Strategy interface for WhatsApp notification providers.
 *
 * Implementations:
 *  - MockWhatsAppProvider   (default — logs to console)
 *  - MetaWhatsAppProvider   (Meta Business Cloud API)
 *  - TwilioWhatsAppProvider (Twilio WhatsApp API)
 *
 * To switch providers, set the environment variable:
 *   WHATSAPP_PROVIDER=mock | meta | twilio
 */
public interface WhatsAppProvider {

    /**
     * Sends a free-form text WhatsApp message.
     * Only works within the 24-hour customer service window.
     *
     * @param phone   recipient phone (E.164 format, e.g. 919876543210)
     * @param message plain-text message body
     * @return Meta message ID (wamid.xxx) if available, null otherwise
     */
    String sendMessage(String phone, String message);

    /**
     * Sends a pre-approved WhatsApp template message.
     * Works even outside the 24-hour customer service window.
     *
     * @param phone        recipient phone (E.164 format, e.g. 919876543210)
     * @param templateName the name of the approved template in Meta Business Manager
     * @param parameters   map of placeholder names to values for template variables
     * @return Meta message ID (wamid.xxx) if available, null otherwise
     */
    String sendTemplateMessage(String phone, String templateName, Map<String, String> parameters);

    /**
     * Returns the name of this provider (used in logs and history).
     */
    String getProviderName();
}
