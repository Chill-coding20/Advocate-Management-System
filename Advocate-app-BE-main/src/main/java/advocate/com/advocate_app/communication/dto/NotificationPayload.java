package advocate.com.advocate_app.communication.dto;

import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;

import java.util.List;
import java.util.Map;

public class NotificationPayload {

    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationChannel channel;
    private NotificationType type;
    private String subject;
    private String title;
    private String message;
    private Map<String, String> variables;
    private List<String> attachments;
    private Long caseId;
    private Long clientId;
    private Long invoiceId;
    private String priority;
    private String advocateEmail;
    private Advocate advocate;
    private CaseEntity caseEntity;
    private Client client;
    private String emailBody;
    private String whatsappMessage;
    private String whatsappTemplateName;
    private Map<String, String> whatsappTemplateParameters;

    public NotificationPayload() {}

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, String> getVariables() { return variables; }
    public void setVariables(Map<String, String> variables) { this.variables = variables; }
    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getAdvocateEmail() { return advocateEmail; }
    public void setAdvocateEmail(String advocateEmail) { this.advocateEmail = advocateEmail; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getEmailBody() { return emailBody; }
    public void setEmailBody(String emailBody) { this.emailBody = emailBody; }
    public String getWhatsappMessage() { return whatsappMessage; }
    public void setWhatsappMessage(String whatsappMessage) { this.whatsappMessage = whatsappMessage; }
    public String getWhatsappTemplateName() { return whatsappTemplateName; }
    public void setWhatsappTemplateName(String whatsappTemplateName) { this.whatsappTemplateName = whatsappTemplateName; }
    public Map<String, String> getWhatsappTemplateParameters() { return whatsappTemplateParameters; }
    public void setWhatsappTemplateParameters(Map<String, String> whatsappTemplateParameters) { this.whatsappTemplateParameters = whatsappTemplateParameters; }
}
