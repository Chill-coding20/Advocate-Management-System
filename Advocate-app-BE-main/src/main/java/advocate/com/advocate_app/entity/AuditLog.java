package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_advocate", columnList = "advocateId"),
    @Index(name = "idx_audit_created", columnList = "createdAt"),
    @Index(name = "idx_audit_action", columnList = "actionType"),
    @Index(name = "idx_audit_module", columnList = "module"),
    @Index(name = "idx_audit_advocate_created", columnList = "advocateId, createdAt"),
    @Index(name = "idx_audit_advocate_action", columnList = "advocateId, actionType"),
    @Index(name = "idx_audit_advocate_module", columnList = "advocateId, module")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long advocateId;
    private String userName;

    @Column(nullable = false, length = 50)
    private String actionType;

    @Column(length = 50)
    private String module;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 50)
    private String entityType;

    private Long entityId;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 200)
    private String device;

    @Column(length = 200)
    private String browser;

    @Column(length = 100)
    private String operatingSystem;

    @Column(length = 10)
    private String requestMethod;

    @Column(length = 500)
    private String requestUri;

    @Column(length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdvocateId() { return advocateId; }
    public void setAdvocateId(Long advocateId) { this.advocateId = advocateId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
