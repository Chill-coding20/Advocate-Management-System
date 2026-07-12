package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "demo_workspace")
public class DemoWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long advocateId;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private boolean isActive = true;

    private String version;

    @Column(columnDefinition = "TEXT")
    private String recordSummary;

    private String createdBy;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (generatedAt == null) generatedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdvocateId() { return advocateId; }
    public void setAdvocateId(Long advocateId) { this.advocateId = advocateId; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getRecordSummary() { return recordSummary; }
    public void setRecordSummary(String recordSummary) { this.recordSummary = recordSummary; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
