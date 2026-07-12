package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.entity.CommunicationSettings;
import advocate.com.advocate_app.communication.repository.CommunicationSettingsRepository;
import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailSignatureService {

    private final CommunicationSettingsRepository settingsRepository;

    public EmailSignatureService(CommunicationSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public String appendSignature(String htmlBody, Advocate advocate) {
        if (advocate == null) return htmlBody;

        Optional<CommunicationSettings> optSettings = settingsRepository.findByAdvocate(advocate);
        String signature = null;
        String advocateName = advocate.getFullName();
        String barCouncilId = advocate.getBarCouncilId();
        String phone = advocate.getPhone();
        String email = advocate.getEmail();
        String officeAddress = null;
        String website = null;

        if (optSettings.isPresent()) {
            CommunicationSettings s = optSettings.get();
            if (s.getEmailSignature() != null && !s.getEmailSignature().isBlank()) {
                signature = s.getEmailSignature();
            }
            officeAddress = s.getOfficeAddress();
            website = s.getWebsite();
        }

        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<br><br>");
            sb.append("Regards,<br>");
            sb.append("<strong>").append(escapeHtml(advocateName)).append("</strong><br>");
            if (barCouncilId != null && !barCouncilId.isBlank()) {
                sb.append("Bar Council ID: ").append(escapeHtml(barCouncilId)).append("<br>");
            }
            if (phone != null && !phone.isBlank()) {
                sb.append("Phone: ").append(escapeHtml(phone)).append("<br>");
            }
            if (email != null && !email.isBlank()) {
                sb.append("Email: ").append(escapeHtml(email)).append("<br>");
            }
            if (officeAddress != null && !officeAddress.isBlank()) {
                sb.append("Address: ").append(escapeHtml(officeAddress)).append("<br>");
            }
            if (website != null && !website.isBlank()) {
                sb.append("Web: ").append(escapeHtml(website)).append("<br>");
            }
            signature = sb.toString();
        } else {
            signature = "<br><br>" + signature.replace("\n", "<br>");
        }

        int bodyEnd = htmlBody.lastIndexOf("</body>");
        if (bodyEnd > 0) {
            return htmlBody.substring(0, bodyEnd) + signature + "</body></html>";
        }
        return htmlBody + signature;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
