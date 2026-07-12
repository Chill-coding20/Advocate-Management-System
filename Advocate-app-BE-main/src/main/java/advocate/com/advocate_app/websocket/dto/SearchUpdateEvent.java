package advocate.com.advocate_app.websocket.dto;

public class SearchUpdateEvent {
    private String entityType;
    private String action;
    private Object entity;
    private long timestamp;

    public SearchUpdateEvent() {}

    public SearchUpdateEvent(String entityType, String action, Object entity, long timestamp) {
        this.entityType = entityType;
        this.action = action;
        this.entity = entity;
        this.timestamp = timestamp;
    }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Object getEntity() { return entity; }
    public void setEntity(Object entity) { this.entity = entity; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
