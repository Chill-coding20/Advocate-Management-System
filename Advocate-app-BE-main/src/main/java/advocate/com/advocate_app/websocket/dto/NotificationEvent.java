package advocate.com.advocate_app.websocket.dto;

public class NotificationEvent {
    private String type;
    private String message;
    private String entityType;
    private Long entityId;
    private String route;
    private String timestamp;

    public NotificationEvent() {}

    public NotificationEvent(String type, String message, String entityType, Long entityId, String route, String timestamp) {
        this.type = type;
        this.message = message;
        this.entityType = entityType;
        this.entityId = entityId;
        this.route = route;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
