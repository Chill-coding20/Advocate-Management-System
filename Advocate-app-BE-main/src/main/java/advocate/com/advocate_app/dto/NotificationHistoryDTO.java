package advocate.com.advocate_app.dto;

import java.time.LocalDateTime;

/**
 * DTO for NotificationHistory records returned by the Notification Center API.
 */
public class NotificationHistoryDTO {
    private Long id;
    private String eventType;
    private String channel;         // EMAIL | WHATSAPP | IN_APP
    private String status;          // SENT | FAILED | PENDING
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private String subject;
    private String body;
    private String errorMessage;
    private Integer statusCode;
    private String responseBody;
    private String metaMessageId;
    private LocalDateTime sentAt;
    private Long caseId;
    private String caseNumber;
    private Long clientId;
    private String clientName;

    // ----- Getters & Setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public String getMetaMessageId() { return metaMessageId; }
    public void setMetaMessageId(String metaMessageId) { this.metaMessageId = metaMessageId; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
}
