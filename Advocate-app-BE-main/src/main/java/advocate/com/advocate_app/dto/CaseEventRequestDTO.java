package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class CaseEventRequestDTO {
    @NotBlank(message = "Event title is required")
    private String title;

    @NotBlank(message = "Event type is required")
    private String eventType;

    private String description;

    @NotNull(message = "Event date is required")
    private LocalDate date;

    private LocalTime time;

    private CaseIdWrapper caseEntity;

    public static class CaseIdWrapper {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

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

    public CaseIdWrapper getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseIdWrapper caseEntity) { this.caseEntity = caseEntity; }

    public Long getCaseId() {
        return caseEntity != null ? caseEntity.getId() : null;
    }
}
