package advocate.com.advocate_app.communication.entity;

import advocate.com.advocate_app.entity.Advocate;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs", indexes = {
    @Index(name = "idx_notiflog_advocate", columnList = "advocate_id"),
    @Index(name = "idx_notiflog_advocate_created", columnList = "advocate_id, created_at")
})
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advocate_id", nullable = false)
    private Advocate advocate;

    @Column(length = 255)
    private String recipient;

    @Column(length = 100)
    private String eventType;

    @Column(length = 50)
    private String channel;

    @Column(length = 50, nullable = false)
    private String logLevel;

    @Column(length = 500, nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public NotificationLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
