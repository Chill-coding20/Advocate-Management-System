package advocate.com.advocate_app.service;

import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.communication.service.CommunicationDispatcher;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.CaseRepository;
import advocate.com.advocate_app.repository.ClientRepository;
import advocate.com.advocate_app.repository.DocumentRepository;
import advocate.com.advocate_app.storage.DocumentStorageService;
import advocate.com.advocate_app.storage.FileType;
import advocate.com.advocate_app.storage.FileValidationService;
import advocate.com.advocate_app.storage.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentStorageService storageService;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private CommunicationDispatcher notificationDispatcher;

    @Autowired
    private EmailTemplateService templateService;

    @Autowired
    private CaseTimelineService timelineService;

    @Autowired
    private AuditLogService auditLogService;

    public static final List<String> VALID_CATEGORIES = Arrays.asList(
        "Court Order", "Petition", "Evidence", "Agreement", "Affidavit",
        "Notice", "Judgment", "Invoice", "Payment Receipt",
        "Identity Proof", "Address Proof", "Other"
    );

    public Document uploadDocument(String email, MultipartFile file, Long caseId, Long clientId,
                                    String documentName, String category, String description) throws IOException {
        log.info("=== DOCUMENT UPLOAD START ===");
        log.info("STEP 1 — Parameters: file={}, originalName={}, size={}, contentType={}, caseId={}, clientId={}, documentName={}, category={}, description={}",
                file.getOriginalFilename(), file.getOriginalFilename(), file.getSize(), file.getContentType(),
                caseId, clientId, documentName, category, description);

        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("STEP 1 FAILED — Advocate not found for email: {}", email);
                    return new RuntimeException("Advocate not found");
                });
        log.info("STEP 1 PASS — Advocate found: id={}, email={}", advocate.getId(), advocate.getEmail());

        FileType validatedType;
        try {
            validatedType = fileValidationService.validate(file);
            log.info("STEP 2 PASS — File validation: type={}, size={}, contentType={}",
                    validatedType.getExtension(), file.getSize(), file.getContentType());
        } catch (RuntimeException e) {
            log.error("STEP 2 FAILED — File validation rejected: {}", e.getMessage());
            throw e;
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed_file";
        }
        log.info("STEP 3 — Original filename: {}", originalName);

        // Check for existing document with same name for versioning
        Optional<Document> existing = documentRepository
                .findTopByOriginalNameAndAdvocateOrderByVersionDesc(originalName, advocate);

        int newVersion = 1;
        if (existing.isPresent()) {
            newVersion = existing.get().getVersion() + 1;
            log.info("STEP 4 — Versioning: existing document found (v{}), new version = {}", existing.get().getVersion(), newVersion);
        } else {
            log.info("STEP 4 — Versioning: no existing document, new version = {}", newVersion);
        }

        // Determine storage sub-directory
        String subDir = "documents";
        if (category != null) {
            subDir = "documents/" + category.toLowerCase().replaceAll("\\s+", "_");
        }
        log.info("STEP 5 — Storage sub-directory: {}", subDir);

        StoredFile stored;
        try {
            stored = storageService.store(file, subDir);
            log.info("STEP 5 PASS — File stored: storedName={}, filePath={}, fileSize={}, fileType={}",
                    stored.getStoredName(), stored.getFilePath(), stored.getFileSize(), stored.getFileType());
        } catch (IOException e) {
            log.error("STEP 5 FAILED — IOException while storing file: {}", e.getMessage(), e);
            throw e;
        }

        Document doc = new Document();
        doc.setDocumentName(documentName != null && !documentName.isBlank() ? documentName : originalName);
        doc.setOriginalName(originalName);
        doc.setStoredName(stored.getStoredName());
        doc.setFilePath(stored.getFilePath());
        doc.setFileSize(stored.getFileSize());
        doc.setFileType(stored.getFileType());
        doc.setCategory(category != null ? category : "Other");
        doc.setDescription(description);
        doc.setVersion(newVersion);
        doc.setDownloadCount(0);
        doc.setStatus("ACTIVE");
        doc.setUploadDate(LocalDateTime.now());
        doc.setAdvocate(advocate);
        log.info("STEP 6 — Document entity populated: documentName={}, originalName={}, storedName={}, filePath={}, category={}, version={}",
                doc.getDocumentName(), doc.getOriginalName(), doc.getStoredName(), doc.getFilePath(), doc.getCategory(), doc.getVersion());

        if (caseId != null) {
            log.info("STEP 7a — Looking up case: caseId={}", caseId);
            CaseEntity caseEntity = caseRepository.findById(caseId)
                    .orElseThrow(() -> {
                        log.error("STEP 7a FAILED — Case not found: caseId={}", caseId);
                        return new RuntimeException("Case not found");
                    });
            if (!caseEntity.getAdvocate().getId().equals(advocate.getId())) {
                log.error("STEP 7a FAILED — Unauthorized: case caseId={} belongs to advocate {} but user is {}",
                        caseId, caseEntity.getAdvocate().getId(), advocate.getId());
                throw new RuntimeException("Unauthorized: case does not belong to this advocate");
            }
            log.info("STEP 7a PASS — Case found: id={}, caseNumber={}, advocateId={}",
                    caseEntity.getId(), caseEntity.getCaseNumber(), caseEntity.getAdvocate().getId());
            doc.setCaseEntity(caseEntity);
            // Inherit client from case if not explicitly set
            if (clientId == null && caseEntity.getClient() != null) {
                doc.setClient(caseEntity.getClient());
                log.info("STEP 7a — Client inherited from case: clientId={}, name={}", caseEntity.getClient().getId(), caseEntity.getClient().getName());
            }
        }

        if (clientId != null) {
            log.info("STEP 7b — Looking up client: clientId={}", clientId);
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> {
                        log.error("STEP 7b FAILED — Client not found: clientId={}", clientId);
                        return new RuntimeException("Client not found");
                    });
            if (!client.getAdvocate().getId().equals(advocate.getId())) {
                log.error("STEP 7b FAILED — Unauthorized: client clientId={} belongs to advocate {} but user is {}",
                        clientId, client.getAdvocate().getId(), advocate.getId());
                throw new RuntimeException("Unauthorized: client does not belong to this advocate");
            }
            log.info("STEP 7b PASS — Client found: id={}, name={}, advocateId={}",
                    client.getId(), client.getName(), client.getAdvocate().getId());
            doc.setClient(client);
        }

        Document saved;
        try {
            saved = documentRepository.save(doc);
            log.info("STEP 8 PASS — Document saved to database: id={}, documentName={}, version={}",
                    saved.getId(), saved.getDocumentName(), saved.getVersion());
        } catch (Exception e) {
            log.error("STEP 8 FAILED — Database save failed: {}", e.getMessage(), e);
            throw e;
        }

        activityService.logActivity("Document \"" + doc.getDocumentName() + "\" uploaded (v" + newVersion + ")", "DOCUMENT_UPLOADED", advocate);

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.DOCUMENT_UPLOADED, AuditLogService.MODULE_DOCUMENTS,
                    "Document Uploaded",
                    saved.getDocumentName() + " uploaded" + (saved.getCategory() != null ? " (" + saved.getCategory() + ")" : ""),
                    "Document", saved.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        // Record timeline event
        try {
            Long caseIdForTimeline = saved.getCaseEntity() != null ? saved.getCaseEntity().getId() : null;
            Long clientIdForTimeline = saved.getClient() != null ? saved.getClient().getId()
                    : (saved.getCaseEntity() != null && saved.getCaseEntity().getClient() != null ? saved.getCaseEntity().getClient().getId() : null);
            if (caseIdForTimeline != null) {
                String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
                timelineService.recordEvent(
                        caseIdForTimeline, clientIdForTimeline, advocate.getId(),
                        CaseTimelineService.DOCUMENT_UPLOADED,
                        "Document Uploaded",
                        saved.getDocumentName() + " uploaded" + (saved.getCategory() != null ? " (" + saved.getCategory() + ")" : ""),
                        performedBy, "Document", saved.getId()
                );
            }
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Send DOCUMENT_UPLOADED notification to client if linked to a case
        try {
            Client targetClient = saved.getClient();
            if (targetClient == null && saved.getCaseEntity() != null) {
                targetClient = saved.getCaseEntity().getClient();
            }
            if (targetClient != null && targetClient.getEmail() != null) {
                String caseNum = saved.getCaseEntity() != null ? saved.getCaseEntity().getCaseNumber() : "N/A";
                NotificationPayload payload = new NotificationPayload();
                payload.setType(NotificationType.DOCUMENT_UPLOADED);
                payload.setRecipientName(targetClient.getName());
                payload.setRecipientEmail(targetClient.getEmail());
                payload.setRecipientPhone(targetClient.getPhone());
                payload.setAdvocate(advocate);
                payload.setCaseEntity(saved.getCaseEntity());
                payload.setClient(targetClient);
                payload.setSubject("New Document Uploaded — " + caseNum);
                payload.setEmailBody(templateService.documentUploadedEmail(
                        targetClient.getName(), caseNum,
                        saved.getDocumentName(), saved.getCategory(), "/documents/" + saved.getId()));
                payload.setWhatsappMessage(templateService.documentUploadedWhatsApp(
                        targetClient.getName(), caseNum,
                        saved.getDocumentName(), saved.getCategory()));
                payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_DOCUMENT_UPLOADED);
                Map<String, String> templateParams = new HashMap<>();
                templateParams.put("1", targetClient.getName());
                templateParams.put("2", caseNum);
                templateParams.put("3", saved.getDocumentName());
                payload.setWhatsappTemplateParameters(templateParams);
                notificationDispatcher.dispatchSafely(payload);
            }
        } catch (Exception e) {
            log.warn("Could not dispatch DOCUMENT_UPLOADED notification: {}", e.getMessage());
        }

        log.info("=== DOCUMENT UPLOAD COMPLETE ===");
        return saved;
    }

    public Document getDocumentById(Long id, String email) {
        log.info("DOWNLOAD DEBUG — getDocumentById: id={}, requesterEmail={}", id, email);
        Optional<Document> optDoc = documentRepository.findById(id);
        if (optDoc.isEmpty()) {
            log.error("DOWNLOAD DEBUG — Document NOT FOUND in database: id={}", id);
            throw new RuntimeException("Document not found");
        }
        Document doc = optDoc.get();
        log.info("DOWNLOAD DEBUG — Document FOUND: id={}, storedName={}, filePath={}, ownerEmail={}, originalName={}",
                doc.getId(), doc.getStoredName(), doc.getFilePath(), doc.getAdvocate().getEmail(), doc.getOriginalName());
        if (!doc.getAdvocate().getEmail().equals(email)) {
            log.error("DOWNLOAD DEBUG — Unauthorized access: document owner={}, requester={}",
                    doc.getAdvocate().getEmail(), email);
            throw new RuntimeException("Unauthorized to access this document");
        }
        log.info("DOWNLOAD DEBUG — Ownership verified: docId={} belongs to advocate={}", id, email);
        return doc;
    }

    public Resource getDocumentResource(Long id, String email) throws IOException {
        log.info("DOWNLOAD DEBUG — getDocumentResource: id={}, email={}", id, email);
        Document doc = getDocumentById(id, email);
        log.info("DOWNLOAD DEBUG — getDocumentResource: currentDownloadCount={}, filePath={}",
                doc.getDownloadCount(), doc.getFilePath());

        // Increment download count
        doc.setDownloadCount(doc.getDownloadCount() + 1);
        Document saved = documentRepository.save(doc);
        log.info("DOWNLOAD DEBUG — Download count incremented to: {}", saved.getDownloadCount());

        try {
            Resource resource = storageService.loadAsResource(doc.getFilePath());
            log.info("DOWNLOAD DEBUG — Storage resource loaded successfully");
            return resource;
        } catch (IOException e) {
            log.error("DOWNLOAD DEBUG — Storage loadAsResource threw IOException: message={}", e.getMessage(), e);
            throw e;
        }
    }

    public Resource getDocumentResourceForPreview(Long id, String email) throws IOException {
        Document doc = getDocumentById(id, email);
        return storageService.loadAsResource(doc.getFilePath());
    }

    public List<Document> getMyDocuments(String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return documentRepository.findByAdvocateOrderByUploadDateDesc(advocate);
    }

    public Page<Document> getMyDocumentsPaged(String email, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return documentRepository.findByAdvocate(advocate, pageable);
    }

    public List<Document> searchDocuments(String email, String keyword) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return documentRepository.searchDocuments(advocate, keyword);
    }

    public List<Document> filterDocuments(String email, String category, String status, String fileType) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return documentRepository.filterDocuments(advocate, category, status, fileType);
    }

    public Page<Document> searchAndFilterDocuments(String email, String keyword, String category,
                                                    String status, String fileType, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return documentRepository.searchAndFilterDocuments(advocate, keyword, category, status, fileType, pageable);
    }

    public List<Document> getDocumentsByCase(String email, Long caseId) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        if (!caseEntity.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        return documentRepository.findByAdvocateAndCaseEntity(advocate, caseEntity);
    }

    public List<Document> getDocumentsByClient(String email, Long clientId) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        if (!client.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        return documentRepository.findByAdvocateAndClient(advocate, client);
    }

    public Document updateDocumentMetadata(String email, Long id, String documentName,
                                            String category, String description) {
        Document doc = getDocumentById(id, email);
        if (documentName != null && !documentName.isBlank()) {
            doc.setDocumentName(documentName);
        }
        if (category != null) {
            doc.setCategory(category);
        }
        if (description != null) {
            doc.setDescription(description);
        }
        doc.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(doc);
    }

    public void deleteDocument(Long id, String email) throws IOException {
        Document doc = getDocumentById(id, email);
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        String docName = doc.getDocumentName();
        Long docId = doc.getId();

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.DOCUMENT_DELETED, AuditLogService.MODULE_DOCUMENTS,
                    "Document Deleted",
                    docName + " removed",
                    "Document", docId, "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        // Record timeline before deletion
        try {
            Long caseIdForTimeline = doc.getCaseEntity() != null ? doc.getCaseEntity().getId() : null;
            Long clientIdForTimeline = doc.getClient() != null ? doc.getClient().getId()
                    : (doc.getCaseEntity() != null && doc.getCaseEntity().getClient() != null ? doc.getCaseEntity().getClient().getId() : null);
            if (caseIdForTimeline != null) {
                String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
                timelineService.recordEvent(
                        caseIdForTimeline, clientIdForTimeline, advocate.getId(),
                        CaseTimelineService.DOCUMENT_DELETED,
                        "Document Deleted",
                        docName + " removed",
                        performedBy, "Document", docId
                );
            }
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        storageService.delete(doc.getFilePath());
        documentRepository.delete(doc);
        log.info("Document deleted: {} (id={})", doc.getDocumentName(), id);
    }

    public Map<String, Object> getDocumentStats(String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalDocuments", documentRepository.countByAdvocate(advocate));
        stats.put("totalStorageBytes", documentRepository.sumFileSizeByAdvocate(advocate));

        List<Object[]> categoryCounts = documentRepository.countByCategoryGrouped(advocate);
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        for (Object[] row : categoryCounts) {
            categoryMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("categoryCounts", categoryMap);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> activity = documentRepository.uploadActivitySince(advocate, sevenDaysAgo);
        List<Map<String, Object>> activityList = new ArrayList<>();
        for (Object[] row : activity) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", row[0] != null ? row[0].toString() : null);
            entry.put("count", row[1]);
            activityList.add(entry);
        }
        stats.put("uploadActivity", activityList);

        return stats;
    }
}
