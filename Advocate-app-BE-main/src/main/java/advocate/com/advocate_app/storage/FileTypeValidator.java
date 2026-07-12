package advocate.com.advocate_app.storage;

import advocate.com.advocate_app.exception.FileValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class FileTypeValidator {

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
        byte[] magicBytes = readMagicBytes(file);

        if (magicBytes.length == 0) {
            throw new FileValidationException("Corrupted file: unable to read content.");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) extension = originalName.substring(dot);
        }

        if (extension.isBlank()) {
            throw new FileValidationException("File must have a valid extension.");
        }

        Optional<FileType> extMatch = FileType.fromExtension(extension);
        if (extMatch.isEmpty()) {
            throw new FileValidationException("Unsupported file type: " + extension +
                    ". Allowed: .pdf, .doc, .docx, .jpg, .jpeg, .png, .xls, .xlsx");
        }

        FileType expectedType = extMatch.get();
        List<FileType> magicCandidates = FileType.candidatesByMagicBytes(magicBytes);

        if (magicCandidates.isEmpty()) {
            throw new FileValidationException("Corrupted or invalid file: magic bytes do not match any supported format.");
        }

        boolean magicMatches = magicCandidates.stream().anyMatch(c -> c == expectedType);

        if (!magicMatches) {
            String detectedDesc = magicCandidates.size() == 1
                    ? magicCandidates.get(0).getExtension().toUpperCase()
                    : "a " + String.join("/", magicCandidates.stream()
                        .map(t -> t.getExtension().toUpperCase()).toList());
            throw new FileValidationException("File type mismatch: extension says ." + expectedType.getExtension() +
                    " but content looks like " + detectedDesc + ".");
        }

        String suppliedMime = file.getContentType();
        if (suppliedMime != null && !suppliedMime.isBlank()) {
            boolean mimeMatch = magicCandidates.stream().anyMatch(c -> c.getMimeType().equalsIgnoreCase(suppliedMime));
            if (!mimeMatch) {
                throw new FileValidationException("File type mismatch: MIME type '" + suppliedMime +
                        "' does not match detected content type.");
            }
        }

        return expectedType;
    }
}
