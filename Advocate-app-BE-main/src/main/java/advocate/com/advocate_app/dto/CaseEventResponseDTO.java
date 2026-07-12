package advocate.com.advocate_app.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CaseEventResponseDTO {
    private Long id;
    private String title;
    private String eventType;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private boolean notified;
    private CaseEntityInfo caseEntity;

    public static class CaseEntityInfo {
        private Long id;
        private String caseNumber;
        private String caseTitle;

        public CaseEntityInfo() {}
        public CaseEntityInfo(Long id, String caseNumber, String caseTitle) {
            this.id = id;
            this.caseNumber = caseNumber;
            this.caseTitle = caseTitle;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCaseNumber() { return caseNumber; }
        public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

        public String getCaseTitle() { return caseTitle; }
        public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }

    public CaseEntityInfo getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntityInfo caseEntity) { this.caseEntity = caseEntity; }
}
