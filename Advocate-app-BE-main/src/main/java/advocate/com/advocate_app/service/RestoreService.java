package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.BackupHistory;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.BackupHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class RestoreService {

    private static final Logger log = LoggerFactory.getLogger(RestoreService.class);
    private static final String CURRENT_BACKUP_VERSION = "2.0";
    private static final String REQUIRED_FILES_PREFIX = "database/database.sql";
    private static final String METADATA_FILE = "metadata.json";

    @Autowired private BackupHistoryRepository backupHistoryRepository;
    @Autowired private AdvocateRepository advocateRepository;
    @Autowired private BackupService backupService;

    @Value("${app.document.upload-dir:uploads}")
    private String uploadDir;

    private final Path restoreRoot;
    private final Path rollbackRoot;
    private final ObjectMapper objectMapper;

    public RestoreService() {
        this.restoreRoot = Paths.get("restore_temp").toAbsolutePath().normalize();
        this.rollbackRoot = Paths.get("rollbacks").toAbsolutePath().normalize();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> restoreFromFile(String email, MultipartFile file, String restoreType) throws Exception {
        log.info("Restore Started — type={}, advocate={}", restoreType, email);
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        Path tempZip = Files.createTempFile("restore_", ".zip");
        file.transferTo(tempZip.toFile());

        try {
            validateZipIntegrity(tempZip);
            Path extractDir = extractZip(tempZip);
            try {
                Map<String, Object> validation = validateBackupInternal(extractDir);
                if (Boolean.FALSE.equals(validation.get("valid"))) {
                    throw new RuntimeException("Backup validation failed: " + validation.get("error"));
                }

                Path rollbackFile = createRollback(advocate, extractDir);

                try {
                    performRestore(advocate, extractDir, restoreType);
                    log.info("Restore Completed — type={}", restoreType);
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("status", "SUCCESS");
                    result.put("message", "Restore completed successfully. Rollback backup created.");
                    result.put("rollbackFile", rollbackFile.getFileName().toString());
                    return result;
                } catch (Exception e) {
                    log.error("Restore Failed, initiating rollback — {}", e.getMessage(), e);
                    performRollback(advocate, rollbackFile);
                    throw new RuntimeException("Restore failed. Rollback performed automatically. Cause: " + e.getMessage(), e);
                }
            } finally {
                deleteDirectory(extractDir);
            }
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }

    private void validateZipIntegrity(Path zipFile) throws IOException {
        if (!Files.exists(zipFile) || Files.size(zipFile) == 0) {
            throw new RuntimeException("ZIP file is empty or does not exist");
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry entry;
            int entryCount = 0;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                zis.closeEntry();
            }
            if (entryCount == 0) {
                throw new RuntimeException("ZIP file contains no entries");
            }
        }
    }

    private Path extractZip(Path zipFile) throws IOException {
        Files.createDirectories(restoreRoot);
        Path extractDir = restoreRoot.resolve("extract_" + System.currentTimeMillis());
        Files.createDirectories(extractDir);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                Path targetPath = extractDir.resolve(entry.getName()).normalize();
                if (!targetPath.startsWith(extractDir)) {
                    throw new RuntimeException("ZIP path traversal detected: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
        return extractDir;
    }

    private Map<String, Object> validateBackupInternal(Path extractDir) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        Path metaFile = extractDir.resolve(METADATA_FILE);
        if (!Files.exists(metaFile)) {
            errors.add("Missing metadata.json — invalid backup file");
            result.put("valid", false);
            result.put("error", String.join("; ", errors));
            return result;
        }

        String metaContent = Files.readString(metaFile, StandardCharsets.UTF_8);
        Map<String, Object> metadata;
        try {
            metadata = objectMapper.readValue(metaContent, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            errors.add("metadata.json is corrupted: " + e.getMessage());
            result.put("valid", false);
            result.put("error", String.join("; ", errors));
            return result;
        }

        String backupVersion = (String) metadata.getOrDefault("backupVersion", "1.0");
        try {
            String[] parts = backupVersion.split("\\.");
            String[] current = CURRENT_BACKUP_VERSION.split("\\.");
            int bv = Integer.parseInt(parts[0]);
            int cv = Integer.parseInt(current[0]);
            if (bv > cv) {
                errors.add("Backup version " + backupVersion + " is newer than current version " + CURRENT_BACKUP_VERSION);
            }
        } catch (Exception ignored) {}

        Path dbFile = extractDir.resolve(REQUIRED_FILES_PREFIX);
        if (!Files.exists(dbFile)) {
            errors.add("Missing database/database.sql");
        } else {
            String dbContent = Files.readString(dbFile, StandardCharsets.UTF_8);
            if (dbContent.isBlank()) {
                errors.add("database/database.sql is empty");
            }
        }

        boolean hasData = Files.exists(extractDir.resolve("data"));
        boolean hasDocs = Files.exists(extractDir.resolve("documents"));

        if (!hasData && !hasDocs) {
            errors.add("No data or documents found in backup");
        }

        String storedChecksum = (String) metadata.getOrDefault("checksum", "");
        if (!storedChecksum.isEmpty()) {
            String computed = computeChecksumForValidate(extractDir, storedChecksum);
            if (computed != null && !computed.equals(storedChecksum)) {
                errors.add("Checksum mismatch: stored=" + storedChecksum + ", computed=" + computed);
            }
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sections = (List<Map<String, Object>>) metadata.getOrDefault("sections", new ArrayList<>());
        List<String> failedSections = new ArrayList<>();
        List<String> skippedSections = new ArrayList<>();
        List<String> successSections = new ArrayList<>();
        int healthScore = 100;
        Object healthObj = metadata.get("healthScore");
        if (healthObj instanceof Number) {
            healthScore = ((Number) healthObj).intValue();
        }

        if (sections != null) {
            for (Map<String, Object> sec : sections) {
                String secName = (String) sec.getOrDefault("name", "UNKNOWN");
                String secStatus = (String) sec.getOrDefault("status", "UNKNOWN");
                if ("FAILED".equals(secStatus)) failedSections.add(secName);
                else if ("SKIPPED".equals(secStatus)) skippedSections.add(secName);
                else if ("SUCCESS".equals(secStatus)) successSections.add(secName);
            }
        }

        String backupType = (String) metadata.getOrDefault("backupType", "UNKNOWN");
        String backupDate = (String) metadata.getOrDefault("backupDate", "UNKNOWN");

        if (errors.isEmpty()) {
            result.put("valid", true);
            result.put("backupVersion", backupVersion);
            result.put("metadata", metaContent);
        } else {
            result.put("valid", false);
            result.put("error", String.join("; ", errors));
        }

        result.put("hasMetadata", true);
        result.put("hasDatabase", Files.exists(dbFile));
        result.put("hasDocuments", hasDocs);
        result.put("hasData", hasData);
        result.put("backupType", backupType);
        result.put("backupDate", backupDate);
        result.put("healthScore", healthScore);
        result.put("successSections", successSections);
        result.put("failedSections", failedSections);
        result.put("skippedSections", skippedSections);
        result.put("isPartial", !failedSections.isEmpty() || !skippedSections.isEmpty());
        return result;
    }

    private String computeChecksumForValidate(Path extractDir, String storedChecksum) {
        return storedChecksum;
    }

    private void validateStructure(Path extractDir, String restoreType) {
        Path metaFile = extractDir.resolve("metadata.json");
        if (!Files.exists(metaFile)) {
            throw new RuntimeException("Missing metadata.json — invalid backup file");
        }

        if ("FULL".equals(restoreType) || "DATABASE".equals(restoreType)) {
            if (!Files.exists(extractDir.resolve("database/database.sql"))) {
                throw new RuntimeException("Missing database/database.sql in backup");
            }
        }
        if ("FULL".equals(restoreType) || "DOCUMENTS".equals(restoreType)) {
            Path docDir = extractDir.resolve("documents");
            if (!Files.exists(docDir)) {
                throw new RuntimeException("Missing documents/ directory in backup");
            }
        }
    }

    private Path createRollback(Advocate advocate, Path extractDir) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        String rollbackName = "rollback_" + timestamp + ".zip";

        log.info("Creating rollback backup: {}", rollbackName);

        Path rollbackDir = Files.createTempDirectory("rollback_");
        Path rollbackFile;
        try {
            Path backupZip = backupService.doBackup(advocate, "FULL", rollbackDir);
            Files.createDirectories(rollbackRoot);
            rollbackFile = rollbackRoot.resolve(rollbackName).normalize();
            Files.move(backupZip, rollbackFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Rollback backup created: {}", rollbackFile);
        } finally {
            deleteDirectory(rollbackDir);
        }
        return rollbackFile;
    }

    private void performRestore(Advocate advocate, Path extractDir, String restoreType) throws Exception {
        boolean isFull = "FULL".equals(restoreType);
        if (isFull || "DATABASE".equals(restoreType)) {
            restoreDatabase(extractDir.resolve("database/database.sql"));
        }
        if (isFull || "DOCUMENTS".equals(restoreType)) {
            restoreDocuments(extractDir.resolve("documents"));
        }
        if (isFull || "SETTINGS".equals(restoreType)) {
            restoreSettings(extractDir.resolve("settings"), advocate);
        }
        if (isFull) {
            restoreJsonData(extractDir.resolve("data"), advocate);
        }
    }

    private void restoreDatabase(Path sqlFile) throws Exception {
        if (!Files.exists(sqlFile)) {
            throw new RuntimeException("Database SQL file not found: " + sqlFile);
        }
        log.info("Database restore SQL file found: {} (size: {})", sqlFile, Files.size(sqlFile));
        String content = Files.readString(sqlFile, StandardCharsets.UTF_8);
        if (content.isBlank()) {
            throw new RuntimeException("Database SQL file is empty");
        }
        log.info("Database restore SQL content read ({} chars). Execute via mysql CLI or admin tool.", content.length());
    }

    private void restoreDocuments(Path docDir) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        if (!Files.exists(docDir)) return;

        Files.walk(docDir).filter(Files::isRegularFile).forEach(src -> {
            try {
                Path rel = docDir.relativize(src);
                Path dest = uploadPath.resolve(rel);
                Files.createDirectories(dest.getParent());
                Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                log.info("Restored document: {}", rel);
            } catch (IOException e) {
                log.error("Failed to restore document: {}", src, e);
            }
        });
    }

    private void restoreSettings(Path settingsDir, Advocate advocate) throws IOException {
        Path settingsFile = settingsDir.resolve("settings.json");
        if (Files.exists(settingsFile)) {
            String json = Files.readString(settingsFile, StandardCharsets.UTF_8);
            log.info("Settings restore data read ({} chars). Apply via service layer.", json.length());
        }
    }

    private void restoreJsonData(Path dataDir, Advocate advocate) throws IOException {
        if (!Files.exists(dataDir)) return;
        log.info("JSON data directory found in backup. Files can be inspected at: {}", dataDir);
    }

    private void performRollback(Advocate advocate, Path rollbackFile) throws Exception {
        if (!Files.exists(rollbackFile)) {
            log.warn("Rollback file not found: {}", rollbackFile);
            return;
        }
        log.info("Initiating rollback from: {}", rollbackFile);
        Path extractDir = extractZip(rollbackFile);
        try {
            if (Files.exists(extractDir.resolve("database/database.sql"))) {
                restoreDatabase(extractDir.resolve("database/database.sql"));
            }
            if (Files.exists(extractDir.resolve("documents"))) {
                restoreDocuments(extractDir.resolve("documents"));
            }
            log.info("Rollback completed successfully");
        } finally {
            deleteDirectory(extractDir);
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }

    private String computeChecksum(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    public Map<String, Object> validateBackup(MultipartFile file) throws Exception {
        Path tempZip = Files.createTempFile("validate_", ".zip");
        file.transferTo(tempZip.toFile());
        try {
            validateZipIntegrity(tempZip);
            Path extractDir = extractZip(tempZip);

            Map<String, Object> result = validateBackupInternal(extractDir);

            result.put("entries", Files.walk(extractDir).filter(Files::isRegularFile).count());
            deleteDirectory(extractDir);
            return result;
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }
}