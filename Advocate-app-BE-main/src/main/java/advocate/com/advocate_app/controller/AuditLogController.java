package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.AuditLogResponseDTO;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponseDTO>> getAuditLogs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        return ResponseEntity.ok(
                auditLogService.getAuditLogs(advocate.getId(), page, size,
                        actionType, module, dateFrom, dateTo, search, status)
        );
    }
}
