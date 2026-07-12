package advocate.com.advocate_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Value("${app.document.upload-dir:uploads}")
    private String uploadDir;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("timestamp", Instant.now().toString());

        // Database check
        Map<String, Object> db = new LinkedHashMap<>();
        try {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            jdbc.queryForObject("SELECT 1", Integer.class);
            db.put("status", "UP");
        } catch (Exception e) {
            db.put("status", "DOWN");
            db.put("error", "Cannot connect to database");
        }
        body.put("database", db);

        // Mail check
        Map<String, Object> mail = new LinkedHashMap<>();
        if (mailHost != null && !mailHost.isBlank()) {
            mail.put("status", "CONFIGURED");
            mail.put("host", mailHost);
        } else {
            mail.put("status", "NOT_CONFIGURED");
        }
        body.put("mail", mail);

        // Storage check
        Map<String, Object> storage = new LinkedHashMap<>();
        try {
            File dir = new File(uploadDir);
            if (dir.exists() || dir.mkdirs()) {
                storage.put("status", "UP");
                storage.put("path", dir.getAbsolutePath());
                storage.put("writable", dir.canWrite());
            } else {
                storage.put("status", "DOWN");
                storage.put("error", "Cannot create upload directory");
            }
        } catch (Exception e) {
            storage.put("status", "DOWN");
            storage.put("error", e.getMessage());
        }
        body.put("storage", storage);

        body.put("version", "1.0.0");

        return ResponseEntity.ok(body);
    }
}
