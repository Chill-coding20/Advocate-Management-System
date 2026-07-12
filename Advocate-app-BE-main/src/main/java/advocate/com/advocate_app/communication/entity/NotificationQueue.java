package advocate.com.advocate_app.communication.entity;

import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.entity.Advocate;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_queue", indexes = {
    @Index(name = "idx_notifq_status_nextretry", columnList = "status, next_retry_at"),
    @Index(name = "idx_notifq_advocate_status", columnList = "advocate_id, status")
})
public class NotificationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advocate_id", nullable = false)
    private Advocate advocate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payloadJson;

    private int retryCount = 0;

    private int maxRetries = 4;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime nextRetryAt;

    private LocalDateTime processingStartedAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    public NotificationQueue() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public LocalDateTime getProcessingStartedAt() { return processingStartedAt; }
    public void setProcessingStartedAt(LocalDateTime processingStartedAt) { this.processingStartedAt = processingStartedAt; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
