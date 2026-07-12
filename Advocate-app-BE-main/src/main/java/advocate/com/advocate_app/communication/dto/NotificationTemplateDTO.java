package advocate.com.advocate_app.communication.dto;

import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationType;

import java.time.LocalDateTime;

public class NotificationTemplateDTO {

    private Long id;
    private String name;
    private NotificationChannel channel;
    private NotificationType type;
    private String subjectTemplate;
    private String bodyTemplate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NotificationTemplateDTO() {}

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
