package advocate.com.advocate_app.storage;

public class StoredFile {
    private String storedName;
    private String originalName;
    private String filePath;
    private long fileSize;
    private String fileType;

    public StoredFile(String storedName, String originalName, String filePath, long fileSize, String fileType) {
        this.storedName = storedName;
        this.originalName = originalName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
