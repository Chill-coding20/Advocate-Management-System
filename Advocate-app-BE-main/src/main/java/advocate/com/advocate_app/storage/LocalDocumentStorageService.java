package advocate.com.advocate_app.storage;

import advocate.com.advocate_app.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LocalDocumentStorageService implements DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalDocumentStorageService.class);

    private final Path rootLocation;

    public LocalDocumentStorageService(@Value("${app.document.upload-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("documents"));
            Files.createDirectories(rootLocation.resolve("clients"));
            Files.createDirectories(rootLocation.resolve("cases"));
            log.info("Document storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directories", e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, String subDir) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed_file";
        }

        String extension = resolveCorrectExtension(file, originalName);
        String storedName = UUID.randomUUID().toString() + extension;

        Path targetDir = resolveSecureDirectory(subDir);
        Files.createDirectories(targetDir);
        Path targetPath = targetDir.resolve(storedName).normalize();

        if (!targetPath.startsWith(targetDir)) {
            log.warn("BLOCKED path traversal: attempted subDir={}", subDir);
            throw new FileValidationException("Invalid upload path.");
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("ACCEPTED: stored {} -> {} (type={}, size={})",
                originalName, targetPath, file.getContentType(), file.getSize());

        return new StoredFile(
                storedName,
                originalName,
                targetPath.toString(),
                file.getSize(),
                file.getContentType()
        );
    }

    private String resolveCorrectExtension(MultipartFile file, String originalName) {
        try {
            byte[] magicBytes = FileTypeValidator.readMagicBytes(file);
            List<FileType> candidates = FileType.candidatesByMagicBytes(magicBytes);
            if (!candidates.isEmpty()) {
                return "." + candidates.get(0).getExtension();
            }
        } catch (Exception e) {
            log.warn("Could not read magic bytes, falling back to original extension: {}", e.getMessage());
        }
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) ext = originalName.substring(dot);
        return ext;
    }

    private Path resolveSecureDirectory(String subDir) {
        if (subDir == null || subDir.isBlank()) {
            return rootLocation.resolve("documents");
        }
        String sanitized = subDir
                .replaceAll("\\.\\.", "")
                .replaceAll("[/\\\\]{2,}", "/")
                .replaceAll("^[/\\\\]+", "")
                .replaceAll("^~", "");
        Path resolved = rootLocation.resolve(sanitized).normalize();
        if (!resolved.startsWith(rootLocation)) {
            log.warn("BLOCKED path traversal in subDir: {}", subDir);
            return rootLocation.resolve("documents");
        }
        return resolved;
    }

    @Override
    public Resource loadAsResource(String filePath) throws IOException {
        try {
            Path path = Paths.get(filePath);
            if (!path.isAbsolute()) {
                path = rootLocation.resolve(path).normalize();
            }

            if (!path.startsWith(rootLocation)) {
                log.warn("BLOCKED path traversal attempt on download: {}", filePath);
                throw new IOException("Access denied: file path outside storage root.");
            }

            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("File not found or not readable: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new IOException("File not found: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!path.isAbsolute()) {
            path = rootLocation.resolve(path).normalize();
        }
        if (!path.startsWith(rootLocation)) {
            log.warn("BLOCKED delete path traversal: {}", filePath);
            throw new IOException("Access denied: file path outside storage root.");
        }
        Files.deleteIfExists(path);
        log.info("DELETED: {}", filePath);
    }

    @Override
    public String getStorageRoot() {
        return rootLocation.toString();
    }
}
