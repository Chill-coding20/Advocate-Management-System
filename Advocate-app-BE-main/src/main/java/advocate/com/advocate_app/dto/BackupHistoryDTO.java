package advocate.com.advocate_app.dto;

import java.time.LocalDateTime;

public class BackupHistoryDTO {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String backupType;
    private String status;
    private String checksum;
    private Long durationSeconds;
    private LocalDateTime createdAt;

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

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}