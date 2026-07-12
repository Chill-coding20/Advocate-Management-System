package advocate.com.advocate_app.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private String message;
    private LocalDateTime createdAt;
    private boolean readStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }
}
