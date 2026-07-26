package advocate.com.advocate_app.storage;

import advocate.com.advocate_app.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileTypeValidator {

    private static final Logger log = LoggerFactory.getLogger(FileTypeValidator.class);

    private static final int MAGIC_BYTES_READ_SIZE = 32;

    public static byte[] readMagicBytes(MultipartFile file) throws IOException {
        byte[] buffer = new byte[MAGIC_BYTES_READ_SIZE];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(buffer, 0, MAGIC_BYTES_READ_SIZE);
            if (bytesRead < 0) return new byte[0];
            byte[] result = new byte[bytesRead];
            System.arraycopy(buffer, 0, result, 0, bytesRead);
            return result;
        }
    }

    public static FileType validate(MultipartFile file) throws IOException {
        log.info("UPLOAD DEBUG — FileTypeValidator START");

        byte[] magicBytes = readMagicBytes(file);
        String magicHex = bytesToHex(magicBytes);
        log.info("UPLOAD DEBUG — Magic bytes read: count={}, hex={}", magicBytes.length, magicHex);

        if (magicBytes.length == 0) {
            log.warn("UPLOAD DEBUG — REJECTED in FileTypeValidator: magic bytes length is 0");
            throw new FileValidationException("Corrupted file: unable to read content.");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) extension = originalName.substring(dot);
        }
        log.info("UPLOAD DEBUG — Extension='{}' from originalName='{}'", extension, originalName);

        if (extension.isBlank()) {
            log.warn("UPLOAD DEBUG — REJECTED in FileTypeValidator: no extension in '{}'", originalName);
            throw new FileValidationException("File must have a valid extension.");
        }

        Optional<FileType> extMatch = FileType.fromExtension(extension);
        if (extMatch.isEmpty()) {
            log.warn("UPLOAD DEBUG — REJECTED in FileTypeValidator: unsupported extension '{}'", extension);
            throw new FileValidationException("Unsupported file type: " + extension +
                    ". Allowed: .pdf, .doc, .docx, .jpg, .jpeg, .png, .xls, .xlsx");
        }
        FileType expectedType = extMatch.get();
        log.info("UPLOAD DEBUG — Expected type from extension: {} (mime={}, maxSize={})",
                expectedType.getExtension(), expectedType.getMimeType(), expectedType.getMaxSize());

        List<FileType> magicCandidates = FileType.candidatesByMagicBytes(magicBytes);
        String candidatesStr = magicCandidates.isEmpty() ? "[]" :
                magicCandidates.stream().map(ft -> ft.getExtension().toUpperCase()).collect(Collectors.joining("/"));
        log.info("UPLOAD DEBUG — Magic byte candidates: {}", candidatesStr);

        if (magicCandidates.isEmpty()) {
            log.warn("UPLOAD DEBUG — REJECTED in FileTypeValidator: magic bytes match no known format. hex={}", magicHex);
            throw new FileValidationException("Corrupted or invalid file: magic bytes do not match any supported format.");
        }

        boolean magicMatches = magicCandidates.stream().anyMatch(c -> c == expectedType);
        log.info("UPLOAD DEBUG — Magic matches extension: {}", magicMatches);

        if (!magicMatches) {
            String detectedDesc = magicCandidates.size() == 1
                    ? magicCandidates.get(0).getExtension().toUpperCase()
                    : "a " + candidatesStr;
            log.warn("UPLOAD DEBUG — REJECTED in FileTypeValidator: extension says .{} but content looks like {}",
                    expectedType.getExtension(), detectedDesc);
            throw new FileValidationException("File type mismatch: extension says ." + expectedType.getExtension() +
                    " but content looks like " + detectedDesc + ".");
        }

        String suppliedMime = file.getContentType();
        log.info("UPLOAD DEBUG — Supplied MIME type: '{}'", suppliedMime);
        if (suppliedMime != null && !suppliedMime.isBlank()) {
            boolean mimeMatch = magicCandidates.stream().anyMatch(c -> c.getMimeType().equalsIgnoreCase(suppliedMime));
            log.info("UPLOAD DEBUG — MIME matches candidates: {}", mimeMatch);
            if (!mimeMatch) {
                log.warn("UPLOAD DEBUG — REJECTED in FileTypeValidator: MIME type '{}' does not match detected type. Expected one of: {}",
                        suppliedMime, candidatesStr);
                throw new FileValidationException("File type mismatch: MIME type '" + suppliedMime +
                        "' does not match detected content type.");
            }
        }

        log.info("UPLOAD DEBUG — FileTypeValidator PASS: type={}", expectedType.getExtension());
        return expectedType;
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
