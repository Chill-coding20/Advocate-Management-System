package advocate.com.advocate_app.dto;

import java.time.LocalDateTime;

public class AuditLogResponseDTO {

    private Long id;
    private Long advocateId;
    private String userName;
    private String actionType;
    private String module;
    private String title;
    private String description;
    private String entityType;
    private Long entityId;
    private String ipAddress;
    private String device;
    private String browser;
    private String operatingSystem;
    private String requestMethod;
    private String requestUri;
    private String status;
    private String metadata;
    private LocalDateTime createdAt;

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
