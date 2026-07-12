package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "backup_history", indexes = {
    @Index(name = "idx_backup_advocate", columnList = "advocate_id"),
    @Index(name = "idx_backup_advocate_created", columnList = "advocate_id, created_at")
})
public class BackupHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    private Long fileSize;

    @Column(nullable = false)
    private String backupType;

    @Column(nullable = false)
    private String status;

    private Long durationSeconds;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    private String checksum;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = false)
    private Advocate advocate;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "RUNNING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getBackupType() { return backupType; }
    public void setBackupType(String backupType) { this.backupType = backupType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}
