package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_timeline_event", indexes = {
    @Index(name = "idx_timeline_case", columnList = "caseId"),
    @Index(name = "idx_timeline_created", columnList = "createdAt"),
    @Index(name = "idx_timeline_event_type", columnList = "eventType"),
    @Index(name = "idx_timeline_case_created", columnList = "caseId, createdAt"),
    @Index(name = "idx_timeline_case_type", columnList = "caseId, eventType")
})
public class CaseTimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long caseId;

    private Long clientId;

    private Long advocateId;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 50)
    private String icon;

    @Column(length = 20)
    private String color;

    @Column(length = 50)
    private String referenceType;

    private Long referenceId;

    @Column(length = 100)
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getAdvocateId() { return advocateId; }
    public void setAdvocateId(Long advocateId) { this.advocateId = advocateId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
