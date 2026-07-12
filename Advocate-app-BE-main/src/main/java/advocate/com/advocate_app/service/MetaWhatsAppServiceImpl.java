package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@Profile("prod")
public class MetaWhatsAppServiceImpl implements WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(MetaWhatsAppServiceImpl.class);

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${whatsapp.api.url:https://graph.facebook.com/v23.0}")
    private String apiUrl;

    @Value("${app.notification.whatsapp.meta.phone-number-id:}")
    private String phoneNumberId;

    @Value("${app.notification.whatsapp.meta.access-token:}")
    private String accessToken;

    private void sendMessage(String recipientPhone, String message) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            log.warn("No recipient phone provided");
            return;
        }
        try {
            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", recipientPhone,
                    "type", "text",
                    "text", Map.of("body", message)
            );
            String json = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/" + phoneNumberId + "/messages"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.error("WhatsApp API error: status={} body={}", response.statusCode(), response.body());
            } else {
                log.info("WhatsApp message sent to {}", recipientPhone);
            }
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message", e);
        }
    }

    @Override
    public void sendCaseCreatedMessage(CaseEntity caseEntity) {
        if (caseEntity.getClient() == null) return;
        String msg = String.format(
                "Case %s registered. Title: %s. Advocate: %s.",
                caseEntity.getCaseNumber(), caseEntity.getCaseTitle(),
                caseEntity.getAdvocate().getFullName());
        sendMessage(caseEntity.getClient().getPhone(), msg);
    }

    @Override
    public void sendHearingUpdatedMessage(CaseEventEntity event) {
        if (event.getCaseEntity() == null || event.getCaseEntity().getClient() == null) return;
        String msg = String.format(
                "Hearing for %s on %s at %s.",
                event.getCaseEntity().getCaseNumber(),
                event.getDate(), event.getTime());
        sendMessage(event.getCaseEntity().getClient().getPhone(), msg);
    }

    @Override
    public void sendCaseClosedMessage(CaseEntity caseEntity) {
        if (caseEntity.getClient() == null) return;
        String msg = String.format("Case %s closed.", caseEntity.getCaseNumber());
        sendMessage(caseEntity.getClient().getPhone(), msg);
    }

    @Override
    public void sendInvoiceGeneratedMessage(Invoice invoice) {
        if (invoice.getClient() == null) return;
        String msg = String.format("Invoice %s generated. Amount: Rs.%.2f.",
                invoice.getInvoiceNumber(), invoice.getAmount());
        sendMessage(invoice.getClient().getPhone(), msg);
    }

    @Override
    public void sendPaymentReceivedMessage(ClientPayment payment) {
        if (payment.getClient() == null) return;
        String msg = String.format("Payment of Rs.%.2f received. Ref: %s.",
                payment.getAmount(), payment.getReferenceNumber());
        sendMessage(payment.getClient().getPhone(), msg);
    }
}
