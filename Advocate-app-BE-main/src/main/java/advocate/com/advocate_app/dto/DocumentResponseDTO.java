package advocate.com.advocate_app.dto;

import java.time.LocalDateTime;

public class DocumentResponseDTO {
    private Long id;
    private String documentName;
    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String category;
    private String description;
    private int version;
    private int downloadCount;
    private String status;
    private LocalDateTime uploadDate;
    private LocalDateTime updatedAt;
    private CaseEntityInfo caseEntity;
    private ClientInfo client;

    public static class CaseEntityInfo {
        private Long id;
        private String caseNumber;
        private String caseTitle;

        public CaseEntityInfo() {}
        public CaseEntityInfo(Long id, String caseNumber, String caseTitle) {
            this.id = id; this.caseNumber = caseNumber; this.caseTitle = caseTitle;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCaseNumber() { return caseNumber; }
        public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
        public String getCaseTitle() { return caseTitle; }
        public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }
    }

    public static class ClientInfo {
        private Long id;
        private String name;

        public ClientInfo() {}
        public ClientInfo(Long id, String name) { this.id = id; this.name = name; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public CaseEntityInfo getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntityInfo caseEntity) { this.caseEntity = caseEntity; }

    public ClientInfo getClient() { return client; }
    public void setClient(ClientInfo client) { this.client = client; }
}
