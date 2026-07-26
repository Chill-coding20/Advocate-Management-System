package advocate.com.advocate_app.storage;

import advocate.com.advocate_app.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Pattern;

@Service
public class FileValidationService {

    private static final Logger log = LoggerFactory.getLogger(FileValidationService.class);
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[a-zA-Z0-9._-]+");

    private final long defaultMaxSize;

    public FileValidationService(@Value("${spring.servlet.multipart.max-file-size:25MB}") String maxFileSize) {
        this.defaultMaxSize = parseSize(maxFileSize);
    }

    public FileType validate(MultipartFile file) throws IOException {
        log.info("UPLOAD DEBUG — FileValidation START: file={}, contentType={}, size={}",
                file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getContentType() : "null",
                file != null ? file.getSize() : -1);

        if (file == null || file.isEmpty()) {
            log.warn("UPLOAD DEBUG — REJECTED at STEP 1 (empty/null): file==null={}, isEmpty={}",
                    file == null, file != null && file.isEmpty());
            throw new FileValidationException("Uploaded file is empty.");
        }
        log.info("UPLOAD DEBUG — STEP 1 PASS: file is not empty");

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            log.warn("UPLOAD DEBUG — REJECTED at STEP 2 (no name)");
            throw new FileValidationException("File must have a valid name.");
        }
        log.info("UPLOAD DEBUG — STEP 2 PASS: originalName='{}'", originalName);

        try {
            validateFilename(originalName);
            log.info("UPLOAD DEBUG — STEP 3 PASS: filename validated (no traversal/invalid chars)");
        } catch (FileValidationException e) {
            log.warn("UPLOAD DEBUG — REJECTED at STEP 3 (filename): {}", e.getMessage());
            throw e;
        }

        FileType fileType;
        try {
            fileType = FileTypeValidator.validate(file);
            log.info("UPLOAD DEBUG — STEP 4 PASS: detectedType={}, extension={}, mime={}",
                    fileType.getExtension(), fileType.getExtension(), fileType.getMimeType());
        } catch (FileValidationException e) {
            log.warn("UPLOAD DEBUG — REJECTED at STEP 4 (type validation): {}", e.getMessage());
            throw e;
        }

        try {
            validateFileSize(file, fileType);
            log.info("UPLOAD DEBUG — STEP 5 PASS: fileSize={} within limit of {}MB",
                    file.getSize(), fileType.getMaxSize() / (1024 * 1024));
        } catch (FileValidationException e) {
            log.warn("UPLOAD DEBUG — REJECTED at STEP 5 (file size): {}", e.getMessage());
            throw e;
        }

        log.info("UPLOAD DEBUG — ALL STEPS PASSED: file={}, type={}, size={} bytes, mime={}",
                originalName, fileType.getExtension(), file.getSize(), file.getContentType());
        return fileType;
    }

    void validateFilename(String filename) {
        if (filename.contains("../") || filename.contains("..\\")
                || filename.startsWith("../") || filename.startsWith("..\\")
                || filename.contains("/..") || filename.contains("\\..")) {
            log.warn("REJECTED: path traversal attempt in filename: {}", filename);
            throw new FileValidationException("Invalid upload: filename contains path traversal characters.");
        }

        if (filename.startsWith("/") || filename.startsWith("\\")
                || filename.matches("^[a-zA-Z]:[/\\\\].*")) {
            log.warn("REJECTED: absolute path in filename: {}", filename);
            throw new FileValidationException("Invalid upload: filename must not be an absolute path.");
        }

        String namePart;
        int dot = filename.lastIndexOf('.');
        namePart = dot > 0 ? filename.substring(0, dot) : filename;

        if (!SAFE_FILENAME_PATTERN.matcher(namePart).matches()) {
            log.warn("REJECTED: invalid characters in filename: {}", filename);
            throw new FileValidationException("Invalid upload: filename contains special characters or spaces.");
        }
    }

    void validateFileSize(MultipartFile file, FileType fileType) {
        long maxSize = fileType.getMaxSize();
        if (file.getSize() > maxSize) {
            String typeLabel = fileType.getExtension().toUpperCase();
            long maxMB = maxSize / (1024 * 1024);
            log.warn("REJECTED: oversized {} file ({} bytes, max {} MB)",
                    typeLabel, file.getSize(), maxMB);
            throw new FileValidationException(
                    String.format("File too large: %s files are limited to %d MB.", typeLabel, maxMB));
        }
    }

    private long parseSize(String sizeStr) {
        sizeStr = sizeStr.toUpperCase().replace(" ", "");
        try {
            if (sizeStr.endsWith("MB")) return Long.parseLong(sizeStr.replace("MB", "")) * 1024 * 1024;
            if (sizeStr.endsWith("KB")) return Long.parseLong(sizeStr.replace("KB", "")) * 1024;
            if (sizeStr.endsWith("GB")) return Long.parseLong(sizeStr.replace("GB", "")) * 1024 * 1024 * 1024;
            return Long.parseLong(sizeStr);
        } catch (NumberFormatException e) {
            return 25 * 1024 * 1024L;
        }
    }
}
