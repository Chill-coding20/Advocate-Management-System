package advocate.com.advocate_app.dto;

public class BackupDTO {
    private Long id;
    private String backupType;
    private String status;
    private Long fileSize;
    private String fileName;
    private String message;
    private String progress;
    private Long durationSeconds;

    public BackupDTO() {}

    public BackupDTO(String backupType, String status, String message) {
        this.backupType = backupType;
        this.status = status;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBackupType() { return backupType; }
    public void setBackupType(String backupType) { this.backupType = backupType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getProgress() { return progress; }
    public void setProgress(String progress) { this.progress = progress; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
}