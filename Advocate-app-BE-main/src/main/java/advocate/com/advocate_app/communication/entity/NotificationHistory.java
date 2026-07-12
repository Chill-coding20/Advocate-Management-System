package advocate.com.advocate_app.communication.entity;

import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history", indexes = {
    @Index(name = "idx_notifhist_advocate", columnList = "advocate_id"),
    @Index(name = "idx_notifhist_case", columnList = "case_id"),
    @Index(name = "idx_notifhist_client", columnList = "client_id"),
    @Index(name = "idx_notifhist_advocate_sent", columnList = "advocate_id, sent_at"),
    @Index(name = "idx_notifhist_advocate_channel", columnList = "advocate_id, channel"),
    @Index(name = "idx_notifhist_advocate_status", columnList = "advocate_id, status"),
    @Index(name = "idx_notifhist_metamsgid", columnList = "meta_message_id")
})
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advocate_id", nullable = false)
    private Advocate advocate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(length = 100)
    private String eventType;

    @Column(length = 255)
    private String recipient;

    @Column(length = 255)
    private String recipientName;

    @Column(length = 255)
    private String recipientEmail;

    @Column(length = 50)
    private String recipientPhone;

    @Column(length = 512)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationStatus status;

    @Column(columnDefinition = "TEXT")
    private String providerResponse;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer statusCode;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(length = 255)
    private String metaMessageId;

    private Integer retryCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = true)
    private CaseEntity caseEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(length = 100)
    private String triggeredBy;

    @Column(length = 100)
    private String entity;

    private Long entityId;

    @Column(length = 255)
    private String templateUsed;

    private LocalDateTime failedAt;

    public NotificationHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
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
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public String getMetaMessageId() { return metaMessageId; }
    public void setMetaMessageId(String metaMessageId) { this.metaMessageId = metaMessageId; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
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
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
}
