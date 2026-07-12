package advocate.com.advocate_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "case_events", indexes = {
    @Index(name = "idx_caseevent_advocate", columnList = "advocate_id"),
    @Index(name = "idx_caseevent_case", columnList = "case_id"),
    @Index(name = "idx_caseevent_advocate_date", columnList = "advocate_id, date"),
    @Index(name = "idx_caseevent_date", columnList = "date")
})
public class CaseEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // e.g. "Hearing", "Client Meeting"

    @Column(nullable = false)
    private String eventType; // HEARING / MEETING / PAYMENT_DUE / DOCUMENT

    private String description;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime time;

    private boolean notified;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    @JsonIgnoreProperties({"events", "advocate"})
    private CaseEntity caseEntity;

    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = false)
    @JsonIgnoreProperties({"password", "cases"})
    private Advocate advocate;

    // ---------------- Convenience Constructors ----------------
    public CaseEventEntity() {}

    public CaseEventEntity(String title, String eventType, String description,
                           LocalDate date, LocalTime time,
                           CaseEntity caseEntity, Advocate advocate) {
        this.title = title;
        this.eventType = eventType;
        this.description = description;
        this.date = date;
        this.time = time;
        this.caseEntity = caseEntity;
        this.advocate = advocate;
        this.notified = false;
    }

    // ---------------- Helper Methods ----------------
    public boolean isToday() {
        return LocalDate.now().equals(this.date);
    }

    public boolean isUpcomingWithinDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate future = today.plusDays(days);
        return (this.date.isAfter(today.minusDays(1)) && this.date.isBefore(future.plusDays(1)));
    }

    // ---------------- Lifecycle Hook ----------------
    @PrePersist
    public void prePersist() {
        if (this.date == null) {
            this.date = LocalDate.now();
        }
        this.notified = false;
    }

    // ---------------- Manual Getters & Setters ----------------
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

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}
