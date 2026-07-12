package advocate.com.advocate_app.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentStorageService {
    StoredFile store(MultipartFile file, String subDir) throws IOException;
    Resource loadAsResource(String filePath) throws IOException;
    void delete(String filePath) throws IOException;
    String getStorageRoot();
}
