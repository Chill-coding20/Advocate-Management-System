package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.*;

public interface WhatsAppService {
    void sendCaseCreatedMessage(CaseEntity caseEntity);
    void sendHearingUpdatedMessage(CaseEventEntity event);
    void sendCaseClosedMessage(CaseEntity caseEntity);
    void sendInvoiceGeneratedMessage(Invoice invoice);
    void sendPaymentReceivedMessage(ClientPayment payment);
}
