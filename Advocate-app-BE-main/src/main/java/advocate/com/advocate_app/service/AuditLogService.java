package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.AuditLogResponseDTO;
import advocate.com.advocate_app.entity.AuditLog;
import advocate.com.advocate_app.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository repository;

    public AuditLogResponseDTO recordAction(
            Long advocateId, String userName, String actionType, String module,
            String title, String description, String entityType, Long entityId,
            String status, String ipAddress, String device, String browser,
            String operatingSystem, String requestMethod, String requestUri,
            String metadataJson
    ) {
        AuditLog log = new AuditLog();
        log.setAdvocateId(advocateId);
        log.setUserName(userName);
        log.setActionType(actionType);
        log.setModule(module);
        log.setTitle(title);
        log.setDescription(description);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setStatus(status);
        log.setIpAddress(ipAddress);
        log.setDevice(device);
        log.setBrowser(browser);
        log.setOperatingSystem(operatingSystem);
        log.setRequestMethod(requestMethod);
        log.setRequestUri(requestUri);
        log.setMetadata(metadataJson);

        AuditLog saved = repository.save(log);
        return toDTO(saved);
    }

    /** Convenience: minimal params for simple server-side actions. */
    public AuditLogResponseDTO recordAction(
            Long advocateId, String userName, String actionType, String module,
            String title, String description, String entityType, Long entityId,
            String status) {
        return recordAction(advocateId, userName, actionType, module, title, description,
                entityType, entityId, status, null, null, null, null, null, null, null);
    }

    public Page<AuditLogResponseDTO> getAuditLogs(
            Long advocateId, int page, int size,
            String actionTypeFilter, String moduleFilter,
            LocalDateTime dateFrom, LocalDateTime dateTo,
            String searchText, String statusFilter) {

        Pageable pageable = PageRequest.of(page, size);

        // Build query based on filters
        if (searchText != null && !searchText.isBlank()) {
            return repository.findByAdvocateIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                    advocateId, searchText, pageable).map(this::toDTO);
        }
        if (actionTypeFilter != null && !actionTypeFilter.isBlank()) {
            return repository.findByAdvocateIdAndActionTypeInOrderByCreatedAtDesc(
                    advocateId, java.util.List.of(actionTypeFilter.split(",")), pageable).map(this::toDTO);
        }
        if (moduleFilter != null && !moduleFilter.isBlank()) {
            return repository.findByAdvocateIdAndModuleOrderByCreatedAtDesc(
                    advocateId, moduleFilter, pageable).map(this::toDTO);
        }
        if (dateFrom != null && dateTo != null) {
            return repository.findByAdvocateIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    advocateId, dateFrom, dateTo, pageable).map(this::toDTO);
        }
        if (statusFilter != null && !statusFilter.isBlank()) {
            return repository.findByAdvocateIdAndStatusOrderByCreatedAtDesc(
                    advocateId, statusFilter, pageable).map(this::toDTO);
        }
        return repository.findByAdvocateIdOrderByCreatedAtDesc(advocateId, pageable).map(this::toDTO);
    }

    // ===== Action Type Constants =====
    public static final String LOGIN              = "LOGIN";
    public static final String LOGOUT             = "LOGOUT";
    public static final String FAILED_LOGIN       = "FAILED_LOGIN";
    public static final String PASSWORD_CHANGED   = "PASSWORD_CHANGED";
    public static final String PASSWORD_RESET     = "PASSWORD_RESET";
    public static final String PROFILE_UPDATED    = "PROFILE_UPDATED";
    public static final String CLIENT_CREATED     = "CLIENT_CREATED";
    public static final String CLIENT_UPDATED     = "CLIENT_UPDATED";
    public static final String CLIENT_DELETED     = "CLIENT_DELETED";
    public static final String CASE_CREATED       = "CASE_CREATED";
    public static final String CASE_UPDATED       = "CASE_UPDATED";
    public static final String CASE_STATUS_CHANGED= "CASE_STATUS_CHANGED";
    public static final String CASE_DELETED       = "CASE_DELETED";
    public static final String CASE_RESTORED      = "CASE_RESTORED";
    public static final String HEARING_CREATED    = "HEARING_CREATED";
    public static final String HEARING_UPDATED    = "HEARING_UPDATED";
    public static final String HEARING_RESCHEDULED= "HEARING_RESCHEDULED";
    public static final String DOCUMENT_UPLOADED  = "DOCUMENT_UPLOADED";
    public static final String DOCUMENT_DELETED   = "DOCUMENT_DELETED";
    public static final String EXPENSE_CREATED    = "EXPENSE_CREATED";
    public static final String EXPENSE_UPDATED    = "EXPENSE_UPDATED";
    public static final String EXPENSE_DELETED    = "EXPENSE_DELETED";
    public static final String PAYMENT_RECEIVED   = "PAYMENT_RECEIVED";
    public static final String PAYMENT_UPDATED    = "PAYMENT_UPDATED";
    public static final String PAYMENT_DELETED    = "PAYMENT_DELETED";
    public static final String INVOICE_GENERATED  = "INVOICE_GENERATED";
    public static final String INVOICE_PAID       = "INVOICE_PAID";
    public static final String EMAIL_SENT         = "EMAIL_SENT";
    public static final String WHATSAPP_SENT      = "WHATSAPP_SENT";
    public static final String EXPORT_CSV         = "EXPORT_CSV";
    public static final String EXPORT_EXCEL       = "EXPORT_EXCEL";
    public static final String EXPORT_PDF         = "EXPORT_PDF";
    public static final String SETTINGS_UPDATED   = "SETTINGS_UPDATED";
    public static final String HEARING_DELETED     = "HEARING_DELETED";
    public static final String CLIENT_RESTORED     = "CLIENT_RESTORED";

    // ===== Module Constants =====
    public static final String MODULE_AUTH        = "Authentication";
    public static final String MODULE_PROFILE     = "Profile";
    public static final String MODULE_CLIENTS     = "Clients";
    public static final String MODULE_CASES       = "Cases";
    public static final String MODULE_HEARINGS    = "Hearings";
    public static final String MODULE_DOCUMENTS   = "Documents";
    public static final String MODULE_EXPENSES    = "Expenses";
    public static final String MODULE_PAYMENTS    = "Payments";
    public static final String MODULE_INVOICES    = "Invoices";
    public static final String MODULE_COMMUNICATION = "Communication";
    public static final String MODULE_EXPORTS     = "Exports";
    public static final String MODULE_SETTINGS    = "Settings";

    private AuditLogResponseDTO toDTO(AuditLog log) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setId(log.getId());
        dto.setAdvocateId(log.getAdvocateId());
        dto.setUserName(log.getUserName());
        dto.setActionType(log.getActionType());
        dto.setModule(log.getModule());
        dto.setTitle(log.getTitle());
        dto.setDescription(log.getDescription());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setIpAddress(log.getIpAddress());
        dto.setDevice(log.getDevice());
        dto.setBrowser(log.getBrowser());
        dto.setOperatingSystem(log.getOperatingSystem());
        dto.setRequestMethod(log.getRequestMethod());
        dto.setRequestUri(log.getRequestUri());
        dto.setStatus(log.getStatus());
        dto.setMetadata(log.getMetadata());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
