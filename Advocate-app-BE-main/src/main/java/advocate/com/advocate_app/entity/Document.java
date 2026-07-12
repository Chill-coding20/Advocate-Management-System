package advocate.com.advocate_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_doc_advocate", columnList = "advocate_id"),
    @Index(name = "idx_doc_case", columnList = "case_id"),
    @Index(name = "idx_doc_client", columnList = "client_id"),
    @Index(name = "idx_doc_advocate_upload", columnList = "advocate_id, upload_date"),
    @Index(name = "idx_doc_advocate_category", columnList = "advocate_id, category"),
    @Index(name = "idx_doc_original_advocate", columnList = "original_name, advocate_id"),
    @Index(name = "idx_doc_advocate_status", columnList = "advocate_id, status")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private String filePath;

    private Long fileSize;

    private String fileType;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private int downloadCount = 0;

    private String status;

    @Column(nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = true)
    @JsonIgnoreProperties({"advocate", "client", "events", "documents"})
    private CaseEntity caseEntity;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    @JsonIgnoreProperties({"cases", "documents"})
    private Client client;

    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = false)
    @JsonIgnoreProperties({"password", "cases", "clients", "documents"})
    private Advocate advocate;

    @PrePersist
    public void prePersist() {
        if (uploadDate == null) uploadDate = LocalDateTime.now();
        if (version == 0) version = 1;
        if (downloadCount < 0) downloadCount = 0;
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
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

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}
