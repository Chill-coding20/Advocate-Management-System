package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.BackupDTO;
import advocate.com.advocate_app.entity.BackupHistory;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.BackupService;
import advocate.com.advocate_app.service.RestoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private static final Logger log = LoggerFactory.getLogger(BackupController.class);

    @Autowired private BackupService backupService;
    @Autowired private RestoreService restoreService;

    @PostMapping("/quick")
    public ResponseEntity<BackupDTO> createQuickBackup(@RequestHeader("Authorization") String token) throws Exception {
        String email = JwtUtil.extractEmail(token.substring(7));
        BackupDTO dto = backupService.createBackup(email, "QUICK");
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/full")
    public ResponseEntity<BackupDTO> createFullBackup(@RequestHeader("Authorization") String token) throws Exception {
        String email = JwtUtil.extractEmail(token.substring(7));
        BackupDTO dto = backupService.createBackup(email, "FULL");
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/database")
    public ResponseEntity<BackupDTO> createDatabaseBackup(@RequestHeader("Authorization") String token) throws Exception {
        String email = JwtUtil.extractEmail(token.substring(7));
        BackupDTO dto = backupService.createBackup(email, "DATABASE");
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/documents")
    public ResponseEntity<BackupDTO> createDocumentsBackup(@RequestHeader("Authorization") String token) throws Exception {
        String email = JwtUtil.extractEmail(token.substring(7));
        BackupDTO dto = backupService.createBackup(email, "DOCUMENTS");
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/reports")
    public ResponseEntity<BackupDTO> createReportsBackup(@RequestHeader("Authorization") String token) throws Exception {
        String email = JwtUtil.extractEmail(token.substring(7));
        BackupDTO dto = backupService.createBackup(email, "REPORTS");
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/settings")
    public ResponseEntity<BackupDTO> createSettingsBackup(@RequestHeader("Authorization") String token) throws Exception {
        String email = JwtUtil.extractEmail(token.substring(7));
        BackupDTO dto = backupService.createBackup(email, "SETTINGS");
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String restoreType) throws Exception {
        String email = JwtUtil.extractEmail(token.substring(7));
        Map<String, Object> result = restoreService.restoreFromFile(email, file, restoreType);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateBackup(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) throws Exception {
        String email = JwtUtil.extractEmail(token.substring(7));
        Map<String, Object> result = restoreService.validateBackup(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getBackupHistory(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<BackupHistory> history = backupService.getHistory(email);
        List<Map<String, Object>> result = history.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("fileName", h.getFileName());
            m.put("fileSize", h.getFileSize());
            m.put("backupType", h.getBackupType());
            m.put("status", h.getStatus());
            m.put("checksum", h.getChecksum());
            m.put("durationSeconds", h.getDurationSeconds());
            m.put("metadataJson", h.getMetadataJson());
            m.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBackupStats(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Map<String, Object> stats = backupService.getStorageStats(email);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadBackup(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<BackupHistory> history = backupService.getHistory(email);
        BackupHistory entry = history.stream().filter(h -> h.getId().equals(id)).findFirst()
                .orElseThrow(() -> new RuntimeException("Backup not found"));
        Path filePath = backupService.getBackupFilePath(entry.getFileName());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + entry.getFileName() + "\"")
                        .body(resource);
            }
            return ResponseEntity.status(404).body(null);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBackup(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        backupService.deleteBackup(id, email);
        return ResponseEntity.noContent().build();
    }
}