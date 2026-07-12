package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockWhatsAppServiceImpl implements WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(MockWhatsAppServiceImpl.class);

    @Override
    public void sendCaseCreatedMessage(CaseEntity caseEntity) {
        if (caseEntity.getClient() == null) return;
        String clientPhone = caseEntity.getClient().getPhone();
        String message = String.format(
                "⚖️ Hello %s, case %s has been registered with Advocate %s. Title: %s.",
                caseEntity.getClient().getName(),
                caseEntity.getCaseNumber(),
                caseEntity.getAdvocate().getFullName(),
                caseEntity.getCaseTitle()
        );
        logMessage(clientPhone, message);
    }

    @Override
    public void sendHearingUpdatedMessage(CaseEventEntity event) {
        if (event.getCaseEntity() == null || event.getCaseEntity().getClient() == null) return;
        String clientPhone = event.getCaseEntity().getClient().getPhone();
        String message = String.format(
                "🔔 Hello %s, hearing for Case %s has been scheduled on %s at %s. Venue: %s.",
                event.getCaseEntity().getClient().getName(),
                event.getCaseEntity().getCaseNumber(),
                event.getDate().toString(),
                (event.getTime() != null ? event.getTime().toString() : "N/A"),
                event.getCaseEntity().getCourtLevel()
        );
        logMessage(clientPhone, message);
    }

    @Override
    public void sendCaseClosedMessage(CaseEntity caseEntity) {
        if (caseEntity.getClient() == null) return;
        String clientPhone = caseEntity.getClient().getPhone();
        String message = String.format(
                "✅ Hello %s, case %s (%s) has been successfully closed. Thank you for choosing our practice.",
                caseEntity.getClient().getName(),
                caseEntity.getCaseNumber(),
                caseEntity.getCaseTitle()
        );
        logMessage(clientPhone, message);
    }

    @Override
    public void sendInvoiceGeneratedMessage(Invoice invoice) {
        if (invoice.getClient() == null) return;
        String clientPhone = invoice.getClient().getPhone();
        String message = String.format(
                "💵 Invoice %s has been generated for Case %s. Amount: ₹%,.2f. Due Date: %s.",
                invoice.getInvoiceNumber(),
                invoice.getCaseEntity().getCaseNumber(),
                invoice.getAmount(),
                invoice.getDueDate().toString()
        );
        logMessage(clientPhone, message);
    }

    @Override
    public void sendPaymentReceivedMessage(ClientPayment payment) {
        if (payment.getClient() == null) return;
        String clientPhone = payment.getClient().getPhone();
        String message = String.format(
                "✅ Thank you %s, payment of ₹%,.2f received for Case %s. Ref No: %s.",
                payment.getClient().getName(),
                payment.getAmount(),
                payment.getCaseEntity().getCaseNumber(),
                payment.getReferenceNumber()
        );
        logMessage(clientPhone, message);
    }

    private void logMessage(String phone, String message) {
        log.info("[MOCK WHATSAPP] prepared for phone={} messageLength={}", phone != null ? "***" : "N/A", message != null ? message.length() : 0);
    }
}
