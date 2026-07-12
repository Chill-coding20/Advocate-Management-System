package advocate.com.advocate_app.storage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public enum FileType {
    PDF("pdf", "application/pdf", 20 * 1024 * 1024L,
         new byte[]{0x25, 0x50, 0x44, 0x46}),

    DOC("doc", "application/msword", 20 * 1024 * 1024L,
        new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                   (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1}),

    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 20 * 1024 * 1024L,
         new byte[]{0x50, 0x4B, 0x03, 0x04}),

    XLS("xls", "application/vnd.ms-excel", 20 * 1024 * 1024L,
        new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                   (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1}),

    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 20 * 1024 * 1024L,
         new byte[]{0x50, 0x4B, 0x03, 0x04}),

    JPG("jpg", "image/jpeg", 5 * 1024 * 1024L,
        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),

    JPEG("jpeg", "image/jpeg", 5 * 1024 * 1024L,
         new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),

    PNG("png", "image/png", 5 * 1024 * 1024L,
        new byte[]{ (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

    private final String extension;
    private final String mimeType;
    private final long maxSize;
    private final byte[] magicBytes;

    FileType(String extension, String mimeType, long maxSize, byte[] magicBytes) {
        this.extension = extension;
        this.mimeType = mimeType;
        this.maxSize = maxSize;
        this.magicBytes = magicBytes;
    }

    public String getExtension() { return extension; }
    public String getMimeType() { return mimeType; }
    public long getMaxSize() { return maxSize; }
    public byte[] getMagicBytes() { return magicBytes; }

    public boolean matchesMagicBytes(byte[] fileBytes) {
        if (fileBytes.length < magicBytes.length) return false;
        for (int i = 0; i < magicBytes.length; i++) {
            if (fileBytes[i] != magicBytes[i]) return false;
        }
        return true;
    }

    public static Optional<FileType> fromExtension(String ext) {
        if (ext == null) return Optional.empty();
        String lower = ext.toLowerCase().replaceFirst("^\\.", "");
        return Arrays.stream(values())
                .filter(ft -> ft.extension.equals(lower))
                .findFirst();
    }

    private static final Set<FileType> OLE2_TYPES = Set.of(DOC, XLS);
    private static final Set<FileType> ZIP_OLE_TYPES = Set.of(DOCX, XLSX);

    public static boolean isOle2(byte[] fileBytes) {
        return DOC.matchesMagicBytes(fileBytes);
    }

    public static boolean isZipBased(byte[] fileBytes) {
        return DOCX.matchesMagicBytes(fileBytes);
    }

    public static List<FileType> candidatesByMagicBytes(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) return List.of();
        if (isOle2(fileBytes)) return List.of(DOC, XLS);
        if (isZipBased(fileBytes)) return List.of(DOCX, XLSX);
        for (FileType ft : values()) {
            if (ft != DOC && ft != XLS && ft != DOCX && ft != XLSX && ft.matchesMagicBytes(fileBytes)) {
                return List.of(ft);
            }
        }
        return List.of();
    }
}
