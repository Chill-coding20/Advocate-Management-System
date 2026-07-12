package advocate.com.advocate_app.websocket.dto;

public class DashboardUpdateEvent {
    private long timestamp;

    public DashboardUpdateEvent() {}

    public DashboardUpdateEvent(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
