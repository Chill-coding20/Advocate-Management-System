package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.DocumentResponseDTO;
import advocate.com.advocate_app.entity.Document;
import advocate.com.advocate_app.mapper.DocumentMapper;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentMapper documentMapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("DOCUMENT_UPLOAD")
    public ResponseEntity<DocumentResponseDTO> uploadDocument(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caseId", required = false) Long caseId,
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description) throws IOException {
        String email = JwtUtil.extractEmail(token.substring(7));
        log.info("UPLOAD DEBUG — Controller entry: email={}, file={}, caseId={}, clientId={}, documentName={}, category={}",
                email, file != null ? file.getOriginalFilename() : "null", caseId, clientId, documentName, category);
        try {
            Document doc = documentService.uploadDocument(email, file, caseId, clientId, documentName, category, description);
            log.info("UPLOAD DEBUG — Controller success: docId={}, documentName={}", doc.getId(), doc.getDocumentName());
            return ResponseEntity.ok(documentMapper.toResponseDTO(doc));
        } catch (RuntimeException e) {
            log.error("UPLOAD DEBUG — RuntimeException caught in controller: type={}, message={}",
                    e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping({"/list", "/search"})
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<List<DocumentResponseDTO>> getMyDocuments(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<DocumentResponseDTO> dtos = documentService.getMyDocuments(email).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<Map<String, Object>> getDocumentsPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fileType,
            @RequestParam(defaultValue = "uploadDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Document> docPage = documentService.searchAndFilterDocuments(email, keyword, category, status, fileType, pageable);

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", docPage.getContent().stream().map(documentMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", docPage.getNumber());
        response.put("size", docPage.getSize());
        response.put("totalElements", docPage.getTotalElements());
        response.put("totalPages", docPage.getTotalPages());
        response.put("last", docPage.isLast());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<DocumentResponseDTO> getDocument(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Document doc = documentService.getDocumentById(id, email);
        return ResponseEntity.ok(documentMapper.toResponseDTO(doc));
    }

    @GetMapping("/download/{id}")
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<?> downloadDocument(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        log.info("DOWNLOAD DEBUG — Controller entry: id={}, email={}", id, email);
        try {
            Document doc = documentService.getDocumentById(id, email);
            log.info("DOWNLOAD DEBUG — Document lookup success: id={}, owner={}, filePath={}, fileType={}",
                    doc.getId(), doc.getAdvocate().getEmail(), doc.getFilePath(), doc.getFileType());
            Resource resource = documentService.getDocumentResource(id, email);
            log.info("DOWNLOAD DEBUG — Resource loaded, returning response");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getFileType() != null ? doc.getFileType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("DOWNLOAD DEBUG — IOException: type={}, message={}", e.getClass().getName(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Unable to read the requested file.");
        } catch (RuntimeException e) {
            log.error("DOWNLOAD DEBUG — RuntimeException: type={}, message={}", e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/preview/{id}")
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<?> previewDocument(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        try {
            Document doc = documentService.getDocumentById(id, email);
            Resource resource = documentService.getDocumentResourceForPreview(id, email);
            String contentType = doc.getFileType() != null ? doc.getFileType() : "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Unable to preview the requested file.");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponseDTO>> searchDocuments(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String keyword) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<DocumentResponseDTO> dtos = documentService.searchDocuments(email, keyword).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/filter")
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<List<DocumentResponseDTO>> filterDocuments(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fileType) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<DocumentResponseDTO> dtos = documentService.filterDocuments(email, category, status, fileType).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-case/{caseId}")
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<List<DocumentResponseDTO>> getDocumentsByCase(
            @RequestHeader("Authorization") String token,
            @PathVariable Long caseId) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<DocumentResponseDTO> dtos = documentService.getDocumentsByCase(email, caseId).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-client/{clientId}")
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<List<DocumentResponseDTO>> getDocumentsByClient(
            @RequestHeader("Authorization") String token,
            @PathVariable Long clientId) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<DocumentResponseDTO> dtos = documentService.getDocumentsByClient(email, clientId).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stats")
    @RequirePermission("DOCUMENT_VIEW")
    public ResponseEntity<Map<String, Object>> getDocumentStats(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(documentService.getDocumentStats(email));
    }

    @PutMapping("/{id}")
    @RequirePermission("DOCUMENT_EDIT")
    public ResponseEntity<DocumentResponseDTO> updateDocumentMetadata(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> updates) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Document doc = documentService.updateDocumentMetadata(
                email, id,
                updates.get("documentName"),
                updates.get("category"),
                updates.get("description")
        );
        return ResponseEntity.ok(documentMapper.toResponseDTO(doc));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("DOCUMENT_DELETE")
    public ResponseEntity<Void> deleteDocument(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) throws IOException {
        String email = JwtUtil.extractEmail(token.substring(7));
        documentService.deleteDocument(id, email);
        return ResponseEntity.noContent().build();
    }
}
