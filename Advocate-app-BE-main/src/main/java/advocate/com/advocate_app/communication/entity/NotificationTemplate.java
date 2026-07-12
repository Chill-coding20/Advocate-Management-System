package advocate.com.advocate_app.communication.entity;

import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.entity.Advocate;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_templates", indexes = {
    @Index(name = "idx_notiftpl_advocate", columnList = "advocate_id")
})
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(columnDefinition = "TEXT")
    private String subjectTemplate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String bodyTemplate;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advocate_id", nullable = false)
    private Advocate advocate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public NotificationTemplate() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getSubjectTemplate() { return subjectTemplate; }
    public void setSubjectTemplate(String subjectTemplate) { this.subjectTemplate = subjectTemplate; }
    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
