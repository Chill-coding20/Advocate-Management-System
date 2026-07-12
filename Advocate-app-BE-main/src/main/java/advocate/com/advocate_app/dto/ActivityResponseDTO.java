package advocate.com.advocate_app.dto;

import java.time.LocalDateTime;

public class ActivityResponseDTO {
    private Long id;
    private String description;
    private String actionType;
    private LocalDateTime timestamp;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
