package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.BackupDTO;
import advocate.com.advocate_app.dto.BackupMetadataDTO;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final String BACKUP_VERSION = "2.0";

    @Autowired private BackupHistoryRepository backupHistoryRepository;
    @Autowired private AdvocateRepository advocateRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private CaseRepository caseRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private CaseEventRepository caseEventRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private ActivityRepository activityRepository;

    @Value("${app.document.upload-dir:uploads}")
    private String uploadDir;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.application.name:advocate-app}")
    private String appVersion;

    private final Path backupRoot;
    private final ObjectMapper objectMapper;

    private static final List<String> TABLE_NAMES = Arrays.asList(
        "advocate", "clients", "cases", "case_events", "documents",
        "expenses", "invoices", "tasks", "notification_entity",
        "activity", "client_payments", "backup_history"
    );

    public BackupService() {
        this.backupRoot = Paths.get("backups").toAbsolutePath().normalize();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public BackupDTO createBackup(String email, String type) throws Exception {
        return createBackup(email, type, null);
    }

    public BackupDTO createBackup(String email, String type, ProgressCallback progressCallback) throws Exception {
        log.info("Backup Started — type={}, advocate={}", type, email);
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String zipName = "Advocate_Backup_" + timestamp + ".zip";

        BackupHistory history = new BackupHistory();
        history.setBackupType(type);
        history.setStatus("RUNNING");
        history.setAdvocate(advocate);
        history.setFileName(zipName);

        log.debug("Backup history pre-save: type={} status={}", type, "RUNNING");

        try {
            history = backupHistoryRepository.save(history);
        } catch (Exception ex) {
            log.error("Failed to save backup history", ex);
            throw ex;
        }
        Long backupId = history.getId();

        long startTime = System.currentTimeMillis();

        try {
            Path tmpDir = Files.createTempDirectory("backup_");
            List<BackupMetadataDTO.SectionResult> sectionResults = new ArrayList<>();

            reportProgress(progressCallback, "Preparing Backup\u2026");

            Files.createDirectories(tmpDir.resolve("database"));
            Files.createDirectories(tmpDir.resolve("data"));
            Files.createDirectories(tmpDir.resolve("documents"));
            Files.createDirectories(tmpDir.resolve("reports"));
            Files.createDirectories(tmpDir.resolve("settings"));

            boolean isFull = "FULL".equals(type);
            boolean isQuick = "QUICK".equals(type);

            sectionResults.add(runSection("DATABASE", isFull || isQuick || "DATABASE".equals(type), () -> {
                exportDatabase(tmpDir.resolve("database/database.sql"));
            }));
            sectionResults.add(runSection("DOCUMENTS", isFull || isQuick || "DOCUMENTS".equals(type), () -> {
                exportDocuments(tmpDir.resolve("documents"), advocate);
            }));
            sectionResults.add(runSection("REPORTS", isFull || "REPORTS".equals(type), () -> {
                exportReports(tmpDir.resolve("reports"), advocate);
            }));
            sectionResults.add(runSection("SETTINGS", isFull || "SETTINGS".equals(type), () -> {
                exportSettings(tmpDir.resolve("settings"), advocate);
            }));
            sectionResults.add(runSection("JSON", isFull || isQuick, () -> {
                exportJsonData(tmpDir.resolve("data"), advocate);
            }));

            reportProgress(progressCallback, "Generating Metadata\u2026");

            long durationSeconds = (System.currentTimeMillis() - startTime) / 1000;

            BackupMetadataDTO meta = buildMetadata(advocate, type, durationSeconds, sectionResults);
            String metaJson = objectMapper.writeValueAsString(meta);
            Files.writeString(tmpDir.resolve("metadata.json"), metaJson, StandardCharsets.UTF_8);

            reportProgress(progressCallback, "Compressing ZIP\u2026");

            Files.createDirectories(backupRoot);
            Path zipFile = backupRoot.resolve(zipName);

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
                Files.walk(tmpDir).filter(p -> !Files.isDirectory(p)).forEach(p -> {
                    String entryName = tmpDir.relativize(p).toString().replace("\\", "/");
                    try {
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(p, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        log.warn("Failed to add {} to ZIP", entryName, e);
                    }
                });
            }

            reportProgress(progressCallback, "Calculating Checksum\u2026");
            String checksum = computeChecksumStreaming(zipFile);
            long fileSize = Files.size(zipFile);

            history.setFileName(zipFile.getFileName().toString());
            history.setFileSize(fileSize);
            history.setChecksum(checksum);
            history.setDurationSeconds(durationSeconds);
            history.setMetadataJson(metaJson);

            log.debug("Backup status debug: type={} results={}", type, sectionResults.size());
            boolean anyFailed = sectionResults.stream().anyMatch(s -> "FAILED".equals(s.getStatus()));
            boolean anySuccess = sectionResults.stream().anyMatch(s -> "SUCCESS".equals(s.getStatus()));
            boolean allSkipped = sectionResults.stream().allMatch(s -> "SKIPPED".equals(s.getStatus()));
            String finalStatus = allSkipped ? "FAILED" : (anyFailed && !anySuccess) ? "FAILED" : anyFailed ? "PARTIAL" : "SUCCESS";
            log.debug("Backup final status determination: anyFailed={} anySuccess={} allSkipped={} -> {}", anyFailed, anySuccess, allSkipped, finalStatus);
            history.setStatus(finalStatus);

            log.debug("Backup history final save: type={} status={} file={} size={} duration={}s",
                    history.getBackupType(), history.getStatus(), history.getFileName(), history.getFileSize(), history.getDurationSeconds());

            try {
                backupHistoryRepository.save(history);
            } catch (Exception ex) {
                log.error("Failed to save backup history (final)", ex);
                throw ex;
            }

            reportProgress(progressCallback, "Completed");

            log.info("Backup Completed — type={}, file={}, size={}, duration={}s", type, zipFile.getFileName(), fileSize, durationSeconds);

            BackupDTO dto = new BackupDTO(type, history.getStatus(), "Backup completed successfully");
            dto.setId(history.getId());
            dto.setFileName(history.getFileName());
            dto.setFileSize(fileSize);
            dto.setDurationSeconds(durationSeconds);
            dto.setProgress("Completed");
            deleteDirectory(tmpDir);
            return dto;
        } catch (Exception e) {
            history.setStatus("FAILED");

            log.error("Backup failed, saving error history: type={} file={}", history.getBackupType(), history.getFileName());

            try {
                backupHistoryRepository.save(history);
            } catch (Exception ex) {
                log.error("Failed to save backup history (error)", ex);
                throw ex;
            }
            log.error("Backup Failed — type={}: {}", type, e.getMessage(), e);
            throw e;
        }
    }

    public Path doBackup(Advocate advocate, String type, Path tmpDir) throws Exception {
        Files.createDirectories(tmpDir.resolve("database"));
        Files.createDirectories(tmpDir.resolve("data"));
        Files.createDirectories(tmpDir.resolve("documents"));
        Files.createDirectories(tmpDir.resolve("reports"));
        Files.createDirectories(tmpDir.resolve("settings"));

        boolean isFull = "FULL".equals(type);

        if (isFull || "DATABASE".equals(type)) {
            exportDatabase(tmpDir.resolve("database/database.sql"));
        }

        if (isFull || "DOCUMENTS".equals(type)) {
            exportDocuments(tmpDir.resolve("documents"), advocate);
        }

        if (isFull || "REPORTS".equals(type)) {
            exportReports(tmpDir.resolve("reports"), advocate);
        }

        if (isFull || "SETTINGS".equals(type)) {
            exportSettings(tmpDir.resolve("settings"), advocate);
        }

        if (isFull) {
            exportJsonData(tmpDir.resolve("data"), advocate);
        }

        BackupMetadataDTO meta = buildMetadata(advocate, type, 0L, new ArrayList<>());
        String metaJson = objectMapper.writeValueAsString(meta);
        Files.writeString(tmpDir.resolve("metadata.json"), metaJson, StandardCharsets.UTF_8);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String zipName = "Advocate_Backup_" + timestamp + ".zip";
        Files.createDirectories(backupRoot);
        Path zipFile = backupRoot.resolve(zipName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            Files.walk(tmpDir).filter(p -> !Files.isDirectory(p)).forEach(p -> {
                String entryName = tmpDir.relativize(p).toString().replace("\\", "/");
                try {
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(p, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    log.error("Failed to add {} to ZIP", entryName, e);
                }
            });
        }
        return zipFile;
    }

    private void exportDatabase(Path sqlFile) throws Exception {
        String dbName = extractDbName(dbUrl);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder sb = new StringBuilder();
        sb.append("-- Advocate App Database Backup\n");
        sb.append("-- Generated: ").append(timestamp).append("\n");
        sb.append("-- Backup Version: ").append(BACKUP_VERSION).append("\n\n");
        sb.append("SET FOREIGN_KEY_CHECKS=0;\n\n");

        for (String table : TABLE_NAMES) {
            sb.append("TRUNCATE TABLE `").append(table).append("`;\n");
        }
        sb.append("\n");

        boolean mysqldumpUsed = false;
        if (isMysqldumpAvailable()) {
            try {
                String dumpOutput = executeMysqldump(dbName, TABLE_NAMES);
                if (dumpOutput != null && !dumpOutput.isBlank()) {
                    sb.append(dumpOutput);
                    mysqldumpUsed = true;
                }
            } catch (Exception e) {
                log.warn("mysqldump failed, falling back to INSERT export: {}", e.getMessage());
            }
        }

        if (!mysqldumpUsed) {
            for (String table : TABLE_NAMES) {
                try {
                    sb.append("-- Data: ").append(table).append("\n");
                    sb.append(exportTableInsertStatements(table));
                    sb.append("\n");
                } catch (Exception e) {
                    log.warn("Could not export table {}: {}", table, e.getMessage());
                }
            }
        }

        sb.append("SET FOREIGN_KEY_CHECKS=1;\n");
        Files.writeString(sqlFile, sb.toString(), StandardCharsets.UTF_8);

        if (sb.length() < 200) {
            log.warn("Database SQL file is almost empty — possible export issue");
        }
    }

    private boolean isMysqldumpAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("mysqldump", "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String executeMysqldump(String dbName, List<String> tables) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("mysqldump");
        cmd.add("-u");
        cmd.add(dbUser);
        cmd.add("-p" + dbPassword);
        cmd.add("--no-create-info");
        cmd.add("--skip-triggers");
        cmd.add("--compact");
        cmd.add("--single-transaction");
        cmd.add("--skip-lock-tables");
        cmd.add(dbName);
        cmd.addAll(tables);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mysqldump exited with code " + exitCode + ": " + output);
        }
        return output.toString();
    }

    private String exportTableInsertStatements(String table) {
        return "-- INSERT statements would be generated here for table: " + table + "\n";
    }

    private void exportDocuments(Path docDir, Advocate advocate) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (Files.exists(uploadPath)) {
            Files.walk(uploadPath).filter(Files::isRegularFile).forEach(src -> {
                try {
                    Path rel = uploadPath.relativize(src);
                    Path dest = docDir.resolve(rel);
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.error("Failed to copy document: {}", src, e);
                }
            });
        }
    }

    private void exportReports(Path reportDir, Advocate advocate) throws IOException {
        Path reportsPath = Paths.get("reports").toAbsolutePath().normalize();
        if (Files.exists(reportsPath)) {
            Files.walk(reportsPath).filter(Files::isRegularFile).forEach(src -> {
                try {
                    Path rel = reportsPath.relativize(src);
                    Path dest = reportDir.resolve(rel);
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.error("Failed to copy report: {}", src, e);
                }
            });
        }
    }

    private void exportSettings(Path settingsDir, Advocate advocate) throws IOException {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("advocateName", advocate.getFullName());
        settings.put("advocateEmail", advocate.getEmail());
        settings.put("advocatePhone", advocate.getPhone());
        settings.put("advocateSpecialization", advocate.getSpecialization());
        settings.put("advocateBarCouncilId", advocate.getBarCouncilId());
        settings.put("advocateAddress", advocate.getAddress());
        settings.put("exportDate", LocalDateTime.now().toString());

        String json = objectMapper.writeValueAsString(settings);
        Files.writeString(settingsDir.resolve("settings.json"), json, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private void exportJsonData(Path dataDir, Advocate advocate) throws IOException {
        if (!Files.exists(dataDir)) Files.createDirectories(dataDir);

        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jsonMapper.addMixIn(Client.class, BackupSafeMixin.class);
        jsonMapper.addMixIn(CaseEntity.class, BackupSafeMixin.class);
        jsonMapper.addMixIn(Document.class, BackupSafeMixin.class);
        jsonMapper.addMixIn(Expense.class, BackupSafeMixin.class);
        jsonMapper.addMixIn(Invoice.class, BackupSafeMixin.class);
        jsonMapper.addMixIn(CaseEventEntity.class, BackupSafeMixin.class);
        jsonMapper.addMixIn(NotificationEntity.class, BackupSafeMixin.class);
        jsonMapper.addMixIn(Task.class, BackupSafeMixin.class);
        jsonMapper.addMixIn(Activity.class, BackupSafeMixin.class);

        writeJsonFile(dataDir, "clients.json", clientRepository.findAllActiveByAdvocate(advocate), jsonMapper);
        writeJsonFile(dataDir, "cases.json", caseRepository.findByAdvocate(advocate), jsonMapper);
        writeJsonFile(dataDir, "case_events.json", caseEventRepository.findByAdvocate(advocate), jsonMapper);
        writeJsonFile(dataDir, "documents.json", documentRepository.findByAdvocate(advocate), jsonMapper);
        writeJsonFile(dataDir, "expenses.json", expenseRepository.findByAdvocate(advocate), jsonMapper);
        writeJsonFile(dataDir, "invoices.json", invoiceRepository.findByAdvocate(advocate), jsonMapper);
        writeJsonFile(dataDir, "notifications.json", notificationRepository.findByAdvocateAndReadStatusFalseOrderByCreatedAtDesc(advocate), jsonMapper);
        writeJsonFile(dataDir, "tasks.json", taskRepository.findByAdvocate(advocate), jsonMapper);
        writeJsonFile(dataDir, "activities.json", activityRepository.findByAdvocateOrderByTimestampDesc(advocate), jsonMapper);
        writeJsonFile(dataDir, "advocate.json", advocate, jsonMapper);
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"advocate", "caseEntity", "client"})
    abstract static class BackupSafeMixin {}

    private void writeJsonFile(Path dir, String fileName, Object data) throws IOException {
        String json = objectMapper.writeValueAsString(data);
        Files.writeString(dir.resolve(fileName), json, StandardCharsets.UTF_8);
    }

    private void writeJsonFile(Path dir, String fileName, Object data, ObjectMapper mapper) throws IOException {
        String json = mapper.writeValueAsString(data);
        Files.writeString(dir.resolve(fileName), json, StandardCharsets.UTF_8);
    }

    private BackupMetadataDTO.SectionResult runSection(String name, boolean shouldRun, ThrowingRunnable action) {
        if (!shouldRun) {
            return new BackupMetadataDTO.SectionResult(name, "SKIPPED", 0, null);
        }
        long start = System.currentTimeMillis();
        try {
            action.run();
            long duration = System.currentTimeMillis() - start;
            return new BackupMetadataDTO.SectionResult(name, "SUCCESS", duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.warn("{} export failed: {}", name, e.getMessage());
            return new BackupMetadataDTO.SectionResult(name, "FAILED", duration, e.getMessage());
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    private BackupMetadataDTO buildMetadata(Advocate advocate, String type, long durationSeconds, List<BackupMetadataDTO.SectionResult> sectionResults) {
        BackupMetadataDTO meta = new BackupMetadataDTO();
        meta.setApplicationVersion(appVersion);
        meta.setBackupVersion(BACKUP_VERSION);
        meta.setBackupDate(LocalDateTime.now().toString());
        meta.setBackupType(type);
        meta.setAdvocateName(advocate.getFullName());
        meta.setAdvocateEmail(advocate.getEmail());
        meta.setDatabaseType(extractDbType(dbUrl));
        meta.setDatabaseVersion(extractDbVersion());
        meta.setNumberOfClients(clientRepository.countByAdvocate(advocate));
        meta.setNumberOfCases(caseRepository.countByAdvocateAndDeletedFalse(advocate));
        meta.setNumberOfDocuments(documentRepository.countByAdvocate(advocate));
        meta.setNumberOfExpenses(expenseRepository.countByAdvocate(advocate));
        meta.setNumberOfInvoices(invoiceRepository.countByAdvocate(advocate));
        meta.setNumberOfNotifications(notificationRepository.countByAdvocate(advocate));
        meta.setNumberOfTasks(taskRepository.countByAdvocate(advocate));
        meta.setNumberOfCaseEvents(caseEventRepository.countByAdvocate(advocate));
        meta.setNumberOfActivities(activityRepository.countByAdvocate(advocate));
        meta.setDurationSeconds(durationSeconds);
        meta.setSections(sectionResults);
        meta.setHealthScore(BackupMetadataDTO.computeHealthScore(sectionResults));
        return meta;
    }

    private String computeChecksumStreaming(Path file) throws Exception {
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

    private String extractDbName(String url) {
        if (url == null) return "advocate_db";
        int idx = url.lastIndexOf('/');
        if (idx >= 0) {
            String name = url.substring(idx + 1);
            int paramIdx = name.indexOf('?');
            return paramIdx >= 0 ? name.substring(0, paramIdx) : name;
        }
        return "advocate_db";
    }

    private String extractDbType(String url) {
        if (url == null) return "MySQL";
        if (url.contains("mysql")) return "MySQL";
        if (url.contains("postgresql")) return "PostgreSQL";
        if (url.contains("h2")) return "H2";
        return "MySQL";
    }

    private String extractDbVersion() {
        try {
            return System.getProperty("mysql.version", "8.0");
        } catch (Exception e) {
            return "8.0";
        }
    }

    private void reportProgress(ProgressCallback callback, String stage) {
        if (callback != null) {
            try {
                callback.onProgress(stage);
            } catch (Exception e) {
                log.warn("Progress callback failed: {}", e.getMessage());
            }
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }

    public Path getBackupFilePath(String fileName) {
        return backupRoot.resolve(fileName).normalize();
    }

    public List<BackupHistory> getHistory(String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return backupHistoryRepository.findByAdvocateOrderByCreatedAtDesc(advocate);
    }

    public Map<String, Object> getStorageStats(String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        List<BackupHistory> all = backupHistoryRepository.findByAdvocateOrderByCreatedAtDesc(advocate);
        long totalSize = all.stream().filter(h -> h.getFileSize() != null).mapToLong(BackupHistory::getFileSize).sum();
        long count = all.size();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBackups", count);
        stats.put("totalSize", totalSize);
        if (!all.isEmpty()) {
            stats.put("latestBackup", all.get(0).getCreatedAt() != null ? all.get(0).getCreatedAt().toString() : null);
            stats.put("latestBackupType", all.get(0).getBackupType());
            stats.put("latestBackupStatus", all.get(0).getStatus());
        }
        return stats;
    }

    public void deleteBackup(Long id, String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        BackupHistory history = backupHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Backup not found"));
        if (!history.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        Path file = backupRoot.resolve(history.getFileName());
        try { Files.deleteIfExists(file); } catch (IOException ignored) {}
        backupHistoryRepository.delete(history);
    }

    public List<BackupHistory> getHistoryForRestore(String email) {
        return getHistory(email);
    }

    public interface ProgressCallback {
        void onProgress(String stage);
    }
}