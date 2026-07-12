package advocate.com.advocate_app.communication.dto;

import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.enums.NotificationType;
import java.time.LocalDateTime;

public class NotificationHistoryDTO {
    private Long id;
    private NotificationChannel channel;
    private NotificationType type;
    private String recipient;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private String subject;
    private String message;
    private String body;
    private NotificationStatus status;
    private String providerResponse;
    private String errorMessage;
    private Integer statusCode;
    private String metaMessageId;
    private Integer retryCount;
    private String failureReason;
    private String triggeredBy;
    private String entity;
    private Long entityId;
    private String templateUsed;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime failedAt;

    public NotificationHistoryDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public String getMetaMessageId() { return metaMessageId; }
    public void setMetaMessageId(String metaMessageId) { this.metaMessageId = metaMessageId; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getTemplateUsed() { return templateUsed; }
    public void setTemplateUsed(String templateUsed) { this.templateUsed = templateUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
}
