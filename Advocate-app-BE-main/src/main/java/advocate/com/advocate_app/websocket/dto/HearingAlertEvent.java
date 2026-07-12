package advocate.com.advocate_app.websocket.dto;

public class HearingAlertEvent {
    private Long eventId;
    private String title;
    private String caseNumber;
    private String date;
    private String time;
    private String message;

    public HearingAlertEvent() {}

    public HearingAlertEvent(Long eventId, String title, String caseNumber, String date, String time, String message) {
        this.eventId = eventId;
        this.title = title;
        this.caseNumber = caseNumber;
        this.date = date;
        this.time = time;
        this.message = message;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
