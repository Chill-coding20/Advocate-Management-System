package advocate.com.advocate_app.exception;

import java.util.List;

public class ErrorResponse {
    private String message;
    private int status;
    private long timestamp;
    private List<String> details;

    public ErrorResponse(String message, int status, long timestamp) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }

    public ErrorResponse(String message, int status, long timestamp, List<String> details) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.details = details;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }
}
