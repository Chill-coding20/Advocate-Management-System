package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.exception.DuplicateCaseNumberException;
import advocate.com.advocate_app.communication.service.CommunicationDispatcher;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.CaseRepository;
import advocate.com.advocate_app.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    private final CaseRepository caseRepository;
    private final AdvocateRepository advocateRepository;
    private final ClientRepository clientRepository;

    @Autowired
    private CommunicationDispatcher notificationDispatcher;

    @Autowired
    private EmailTemplateService templateService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private CaseTimelineService timelineService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    public CaseService(CaseRepository caseRepository,
                       AdvocateRepository advocateRepository,
                       ClientRepository clientRepository) {
        this.caseRepository = caseRepository;
        this.advocateRepository = advocateRepository;
        this.clientRepository = clientRepository;
    }

    public Page<CaseEntity> getCasesPaged(String email, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return caseRepository.findByAdvocateAndDeletedFalse(advocate, pageable);
    }

    public Page<CaseEntity> getArchivedCasesPaged(String email, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return caseRepository.findByAdvocateAndDeletedTrue(advocate, pageable);
    }

    public Page<CaseEntity> searchCasesPaged(String email, String keyword, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        if (keyword == null || keyword.isBlank()) {
            return caseRepository.findByAdvocateAndDeletedFalse(advocate, pageable);
        }
        return caseRepository.searchCasesPaged(keyword, advocate, pageable);
    }

    public Page<CaseEntity> searchArchivedCasesPaged(String email, String keyword, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        if (keyword == null || keyword.isBlank()) {
            return caseRepository.findByAdvocateAndDeletedTrue(advocate, pageable);
        }
        return caseRepository.searchArchivedCasesPaged(keyword, advocate, pageable);
    }

    public CaseEntity createCase(String advocateEmail, CaseEntity caseEntity) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        String caseNumber = caseEntity.getCaseNumber();
        if (caseNumber == null || caseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Case number must not be empty.");
        }
        caseEntity.setCaseNumber(caseNumber.trim());

        if (caseRepository.existsByCaseNumber(caseEntity.getCaseNumber())) {
            throw new DuplicateCaseNumberException(
                    "Case number already exists. Please use a different case number.");
        }

        caseEntity.setAdvocate(advocate);

        if (caseEntity.getClient() != null && caseEntity.getClient().getId() != null) {
            Client client = clientRepository.findById(caseEntity.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            if (!client.getAdvocate().getId().equals(advocate.getId())) {
                throw new RuntimeException("Client does not belong to this advocate");
            }
            caseEntity.setClient(client);
        }

        CaseEntity saved;
        try {
            saved = caseRepository.save(caseEntity);
        } catch (DataIntegrityViolationException e) {
            log.warn("Race condition: duplicate case number '{}' hit DB constraint", caseEntity.getCaseNumber());
            throw new DuplicateCaseNumberException(
                    "Case number already exists. Please use a different case number.");
        }

        // Send CASE_CREATED notification to client
        try {
            Client client = saved.getClient();
            if (client != null) {
                Map<String, String> templateParams = new HashMap<>();
                templateParams.put("1", client.getName());
                templateParams.put("2", saved.getCaseNumber());
                templateParams.put("3", saved.getCaseTitle() != null ? saved.getCaseTitle() : "");

                NotificationPayload payload = new NotificationPayload();
                payload.setType(NotificationType.CASE_CREATED);
                payload.setRecipientName(client.getName());
                payload.setRecipientEmail(client.getEmail());
                payload.setRecipientPhone(client.getPhone());
                payload.setAdvocate(advocate);
                payload.setCaseEntity(saved);
                payload.setClient(client);
                payload.setSubject("New Case Registered — " + saved.getCaseNumber());
                payload.setEmailBody(templateService.caseCreatedEmail(client.getName(), saved.getCaseNumber(),
                        saved.getCaseTitle(), advocate.getFullName(), "/cases/" + saved.getId()));
                payload.setWhatsappMessage(templateService.caseCreatedWhatsApp(client.getName(), saved.getCaseNumber(),
                        saved.getCaseTitle(), advocate.getFullName()));
                payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_CASE_CREATED);
                payload.setWhatsappTemplateParameters(templateParams);
                notificationDispatcher.dispatchSafely(payload);
            }
        } catch (Exception e) {
            log.warn("Could not dispatch CASE_CREATED notification: {}", e.getMessage());
        }

        // Record timeline event
        try {
            Long clientId = saved.getClient() != null ? saved.getClient().getId() : null;
            String clientName = saved.getClient() != null ? saved.getClient().getName() : null;
            timelineService.recordEvent(
                    saved.getId(), clientId, advocate.getId(),
                    CaseTimelineService.CASE_CREATED,
                    "Case Created",
                    "Case " + saved.getCaseNumber() + " created" +
                            (clientName != null ? " for " + clientName : ""),
                    advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    "Case", saved.getId()
            );
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log
        try {
            auditLogService.recordAction(
                    advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    AuditLogService.CASE_CREATED, AuditLogService.MODULE_CASES,
                    "Case Created", "Case " + saved.getCaseNumber() + " created",
                    "Case", saved.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        // Auto-create initial invoice if an agreed amount is set
        try {
            if (saved.getClient() != null) {
                invoiceService.createInitialInvoiceForCase(saved);
            }
        } catch (Exception e) {
            log.warn("Could not auto-create initial invoice for case {}: {}", saved.getCaseNumber(), e.getMessage());
        }

        return saved;
    }

    public List<CaseEntity> getMyCases(String advocateEmail) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return caseRepository.findByAdvocateAndDeletedFalse(advocate);
    }

    public List<CaseEntity> getMyArchivedCases(String advocateEmail) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return caseRepository.findByAdvocateAndDeletedTrue(advocate, Pageable.unpaged()).getContent();
    }

    public List<CaseEntity> searchCases(String advocateEmail, String keyword) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        List<CaseEntity> allCases = caseRepository.findByAdvocateAndDeletedFalse(advocate);

        return allCases.stream()
                .filter(c -> (keyword == null || keyword.isBlank()) ||
                        (c.getCaseNumber() != null && c.getCaseNumber().toLowerCase().contains(keyword.toLowerCase())) ||
                        (c.getCaseTitle() != null && c.getCaseTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                        (c.getCaseType() != null && c.getCaseType().toLowerCase().contains(keyword.toLowerCase())) ||
                        (c.getCourtLevel() != null && c.getCourtLevel().toLowerCase().contains(keyword.toLowerCase())) ||
                        (c.getStatus() != null && c.getStatus().toLowerCase().contains(keyword.toLowerCase())) ||
                        (c.getClient() != null && (
                                (c.getClient().getName() != null && c.getClient().getName().toLowerCase().contains(keyword.toLowerCase())) ||
                                        (c.getClient().getEmail() != null && c.getClient().getEmail().toLowerCase().contains(keyword.toLowerCase())) ||
                                        (c.getClient().getPhone() != null && c.getClient().getPhone().toLowerCase().contains(keyword.toLowerCase()))
                        )))
                .toList();
    }

    public CaseEntity updateCase(String advocateEmail, Long caseId, CaseEntity updatedCase) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        CaseEntity existingCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (!existingCase.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("You are not authorized to update this case.");
        }

        String oldStatus = existingCase.getStatus();
        existingCase.setCaseNumber(updatedCase.getCaseNumber());
        existingCase.setCaseTitle(updatedCase.getCaseTitle());
        existingCase.setCaseType(updatedCase.getCaseType());
        existingCase.setCourtLevel(updatedCase.getCourtLevel());
        existingCase.setStatus(updatedCase.getStatus());
        existingCase.setAmount(updatedCase.getAmount());
        existingCase.setDescription(updatedCase.getDescription());

        if (updatedCase.getClient() != null && updatedCase.getClient().getId() != null) {
            Client newClient = clientRepository.findById(updatedCase.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            if (!newClient.getAdvocate().getId().equals(advocate.getId())) {
                throw new RuntimeException("Client does not belong to this advocate");
            }
            existingCase.setClient(newClient);
        } else {
            existingCase.setClient(null);
        }

        CaseEntity saved = caseRepository.save(existingCase);

        // Record timeline event
        try {
            Long clientId = saved.getClient() != null ? saved.getClient().getId() : null;
            String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();

            timelineService.recordEvent(
                    saved.getId(), clientId, advocate.getId(),
                    CaseTimelineService.CASE_UPDATED,
                    "Case Updated",
                    "Case " + saved.getCaseNumber() + " details updated",
                    performedBy, "Case", saved.getId()
            );

            if (updatedCase.getStatus() != null && !updatedCase.getStatus().equals(oldStatus)) {
                boolean closing = "CLOSED".equalsIgnoreCase(updatedCase.getStatus());
                boolean reopening = "CLOSED".equalsIgnoreCase(oldStatus);

                String eventType = closing
                        ? CaseTimelineService.CASE_CLOSED
                        : reopening ? CaseTimelineService.CASE_REOPENED : CaseTimelineService.CASE_STATUS_CHANGED;
                String eventTitle = closing ? "Case Closed"
                        : reopening ? "Case Reopened" : "Status Changed";
                String eventDesc = closing
                        ? "Case " + saved.getCaseNumber() + " closed"
                        : reopening ? "Case " + saved.getCaseNumber() + " reopened"
                        : "Status changed from " + oldStatus + " to " + updatedCase.getStatus();

                timelineService.recordEvent(
                        saved.getId(), clientId, advocate.getId(),
                        eventType, eventTitle, eventDesc,
                        performedBy, "Case", saved.getId()
                );
            }
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.CASE_UPDATED, AuditLogService.MODULE_CASES,
                    "Case Updated", "Case " + saved.getCaseNumber() + " details updated",
                    "Case", saved.getId(), "SUCCESS"
            );

            if (updatedCase.getStatus() != null && !updatedCase.getStatus().equals(oldStatus)) {
                boolean closing = "CLOSED".equalsIgnoreCase(updatedCase.getStatus());
                boolean reopening = "CLOSED".equalsIgnoreCase(oldStatus);
                String auditActionType = closing ? AuditLogService.CASE_STATUS_CHANGED
                        : reopening ? AuditLogService.CASE_STATUS_CHANGED : AuditLogService.CASE_STATUS_CHANGED;
                String auditTitle = closing ? "Case Closed" : reopening ? "Case Reopened" : "Status Changed";
                String auditDesc = closing ? "Case " + saved.getCaseNumber() + " closed"
                        : reopening ? "Case " + saved.getCaseNumber() + " reopened"
                        : "Status changed from " + oldStatus + " to " + updatedCase.getStatus();

                auditLogService.recordAction(
                        advocate.getId(), auditUserName,
                        auditActionType, AuditLogService.MODULE_CASES,
                        auditTitle, auditDesc,
                        "Case", saved.getId(), "SUCCESS"
                );
            }
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        // Send notifications based on new status
        try {
            Client client = saved.getClient();
            if (client != null && updatedCase.getStatus() != null &&
                    !updatedCase.getStatus().equals(oldStatus)) {

                NotificationType eventType = "CLOSED".equalsIgnoreCase(updatedCase.getStatus())
                        ? NotificationType.CASE_CLOSED
                        : NotificationType.CASE_STATUS_UPDATED;

                NotificationPayload payload = new NotificationPayload();
                payload.setType(eventType);
                payload.setRecipientName(client.getName());
                payload.setRecipientEmail(client.getEmail());
                payload.setRecipientPhone(client.getPhone());
                payload.setAdvocate(advocate);
                payload.setCaseEntity(saved);
                payload.setClient(client);
                payload.setWhatsappMessage(null);

                if (eventType == NotificationType.CASE_CLOSED) {
                    payload.setSubject("Case Closed — " + saved.getCaseNumber());
                    payload.setEmailBody(templateService.caseClosedEmail(client.getName(), saved.getCaseNumber(), saved.getCaseTitle(), "/cases/" + saved.getId()));
                    payload.setWhatsappMessage(templateService.caseClosedWhatsApp(client.getName(), saved.getCaseNumber(), saved.getCaseTitle()));
                    Map<String, String> templateParams = new HashMap<>();
                    templateParams.put("1", client.getName());
                    templateParams.put("2", saved.getCaseNumber());
                    templateParams.put("3", saved.getCaseTitle() != null ? saved.getCaseTitle() : "");
                    payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_CASE_CLOSED);
                    payload.setWhatsappTemplateParameters(templateParams);
                } else {
                    payload.setSubject("Case Status Updated — " + saved.getCaseNumber());
                    payload.setEmailBody(templateService.caseStatusUpdatedEmail(client.getName(), saved.getCaseNumber(), oldStatus, updatedCase.getStatus(), "/cases/" + saved.getId()));
                }
                notificationDispatcher.dispatchSafely(payload);
            }
        } catch (Exception e) {
            log.warn("Could not dispatch case status notification: {}", e.getMessage());
        }

        return saved;
    }

    public void deleteCase(String advocateEmail, Long caseId) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        CaseEntity existingCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (!existingCase.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("You are not authorized to delete this case.");
        }

        String caseNumber = existingCase.getCaseNumber();
        existingCase.setDeleted(true);
        caseRepository.save(existingCase);

        try {
            auditLogService.recordAction(
                    advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    AuditLogService.CASE_DELETED, AuditLogService.MODULE_CASES,
                    "Case Archived", "Case " + caseNumber + " archived",
                    "Case", caseId, "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }
    }

    public void restoreCase(String advocateEmail, Long caseId) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        CaseEntity existingCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (!existingCase.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("You are not authorized to restore this case.");
        }

        existingCase.setDeleted(false);
        caseRepository.save(existingCase);

        try {
            auditLogService.recordAction(
                    advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    AuditLogService.CASE_RESTORED, AuditLogService.MODULE_CASES,
                    "Case Restored", "Case " + existingCase.getCaseNumber() + " restored from archive",
                    "Case", caseId, "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }
    }
}
