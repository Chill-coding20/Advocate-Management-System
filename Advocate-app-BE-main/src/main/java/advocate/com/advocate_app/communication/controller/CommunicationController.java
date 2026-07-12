package advocate.com.advocate_app.communication.controller;

import advocate.com.advocate_app.communication.dto.NotificationRequestDTO;
import advocate.com.advocate_app.communication.dto.NotificationTemplateDTO;
import advocate.com.advocate_app.communication.entity.CommunicationSettings;
import advocate.com.advocate_app.communication.entity.NotificationHistory;
import advocate.com.advocate_app.communication.entity.NotificationLog;
import advocate.com.advocate_app.communication.repository.CommunicationSettingsRepository;
import advocate.com.advocate_app.communication.repository.NotificationQueueRepository;
import advocate.com.advocate_app.communication.service.*;
import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/communication")
public class CommunicationController {

    private final CommunicationSettingsRepository settingsRepository;
    private final AdvocateRepository advocateRepository;
    private final CommunicationTemplateService templateService;
    private final CommunicationHistoryService historyService;
    private final CommunicationDispatcher dispatcher;
    private final NotificationQueueRepository queueRepository;
    private final NotificationLogService logService;
    private final NotificationExportService exportService;
    private final advocate.com.advocate_app.communication.service.CommunicationCryptoService cryptoService;

    public CommunicationController(CommunicationSettingsRepository settingsRepository,
                                   AdvocateRepository advocateRepository,
                                   CommunicationTemplateService templateService,
                                   CommunicationHistoryService historyService,
                                   CommunicationDispatcher dispatcher,
                                   NotificationQueueRepository queueRepository,
                                   NotificationLogService logService,
                                   NotificationExportService exportService,
                                   advocate.com.advocate_app.communication.service.CommunicationCryptoService cryptoService) {
        this.settingsRepository = settingsRepository;
        this.advocateRepository = advocateRepository;
        this.templateService = templateService;
        this.historyService = historyService;
        this.dispatcher = dispatcher;
        this.queueRepository = queueRepository;
        this.logService = logService;
        this.exportService = exportService;
        this.cryptoService = cryptoService;
    }

    private Advocate getAdvocate(String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        return advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
    }

    // ==================== SETTINGS ====================

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocate(token);
        CommunicationSettings settings = settingsRepository.findByAdvocate(advocate)
                .orElseGet(() -> {
                    CommunicationSettings s = new CommunicationSettings();
                    s.setAdvocate(advocate);
                    return settingsRepository.save(s);
                });
        CommunicationSettings response = new CommunicationSettings();
        BeanUtils.copyProperties(settings, response);
        response.setEncryptedPassword("");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestHeader("Authorization") String token,
                                            @RequestBody CommunicationSettings updated) {
        Advocate advocate = getAdvocate(token);
        CommunicationSettings settings = settingsRepository.findByAdvocate(advocate)
                .orElseGet(() -> {
                    CommunicationSettings s = new CommunicationSettings();
                    s.setAdvocate(advocate);
                    return s;
                });
        settings.setEmailEnabled(updated.isEmailEnabled());
        settings.setWhatsappEnabled(updated.isWhatsappEnabled());
        settings.setSmtpHost(updated.getSmtpHost());
        settings.setSmtpPort(updated.getSmtpPort());
        settings.setSenderEmail(updated.getSenderEmail());
        settings.setSenderName(updated.getSenderName());
        settings.setReplyToEmail(updated.getReplyToEmail());
        settings.setEmailSignature(updated.getEmailSignature());
        settings.setMaxRetryCount(updated.getMaxRetryCount());
        settings.setRetryDelayMinutes(updated.getRetryDelayMinutes());
        settings.setQueueEnabled(updated.isQueueEnabled());
        settings.setWebsite(updated.getWebsite());
        settings.setOfficeAddress(updated.getOfficeAddress());
        String rawPassword = updated.getEncryptedPassword();
        if (rawPassword != null
                && !rawPassword.isBlank()
                && !"encrypted".equals(rawPassword)) {
            settings.setEncryptedPassword(
                    cryptoService.encrypt(rawPassword));
        }
        settings.setWhatsappPhoneNumberId(updated.getWhatsappPhoneNumberId());
        settings.setWhatsappBusinessAccountId(updated.getWhatsappBusinessAccountId());
        settings.setWhatsappAccessToken(updated.getWhatsappAccessToken());
        CommunicationSettings saved = settingsRepository.save(settings);
        saved.setEncryptedPassword(rawPassword != null && !rawPassword.isBlank() ? "encrypted" : "");
        return ResponseEntity.ok(saved);
    }

    // ==================== TEMPLATES ====================

    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplateDTO>> getTemplates(
            @RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocate(token);
        return ResponseEntity.ok(templateService.getTemplates(advocate));
    }

    @PostMapping("/templates")
    public ResponseEntity<NotificationTemplateDTO> createTemplate(
            @RequestHeader("Authorization") String token,
            @RequestBody NotificationTemplateDTO dto) {
        Advocate advocate = getAdvocate(token);
        return ResponseEntity.ok(templateService.createTemplate(dto, advocate));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<NotificationTemplateDTO> updateTemplate(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody NotificationTemplateDTO dto) {
        Advocate advocate = getAdvocate(token);
        return ResponseEntity.ok(templateService.updateTemplate(id, dto, advocate));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Map<String, String>> deleteTemplate(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Advocate advocate = getAdvocate(token);
        templateService.deleteTemplate(id, advocate);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Template deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== HISTORY ====================

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateRange) {
        Advocate advocate = getAdvocate(token);

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        if (dateRange != null && !dateRange.isBlank()) {
            fromDate = resolveDateRange(dateRange);
        } else {
            if (from != null && !from.isBlank()) {
                fromDate = LocalDate.parse(from).atStartOfDay();
            }
            if (to != null && !to.isBlank()) {
                toDate = LocalDate.parse(to).atTime(LocalTime.MAX);
            }
        }

        if (page != null && size != null) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
            Page<NotificationHistory> result = historyService.filterHistory(advocate, channel, status,
                    eventType, fromDate, toDate, search, pageable);
            return ResponseEntity.ok(result);
        }

        if (search != null && !search.isBlank()) {
            String decodedSearch = URLDecoder.decode(search, StandardCharsets.UTF_8);
            return ResponseEntity.ok(historyService.filterHistory(advocate, channel, status,
                    eventType, fromDate, toDate, decodedSearch));
        }

        if (channel != null || status != null || eventType != null || fromDate != null || toDate != null) {
            return ResponseEntity.ok(historyService.filterHistory(advocate, channel, status,
                    eventType, fromDate, toDate, null));
        }

        return ResponseEntity.ok(historyService.getHistory(advocate));
    }

    // ==================== STATISTICS ====================

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocate(token);
        Map<String, Object> stats = historyService.getStats(advocate);

        stats.put("queuePending", queueRepository.countByAdvocateAndStatus(advocate, NotificationStatus.PENDING));
        stats.put("queueProcessing", queueRepository.countByAdvocateAndStatus(advocate, NotificationStatus.PROCESSING));
        stats.put("queueFailed", queueRepository.countByAdvocateAndStatus(advocate, NotificationStatus.FAILED));

        return ResponseEntity.ok(stats);
    }

    // ==================== EXPORT ====================

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String search) {
        Advocate advocate = getAdvocate(token);

        LocalDateTime fromDate = from != null && !from.isBlank() ? LocalDate.parse(from).atStartOfDay() : null;
        LocalDateTime toDate = to != null && !to.isBlank() ? LocalDate.parse(to).atTime(LocalTime.MAX) : null;

        org.springframework.data.domain.Page<advocate.com.advocate_app.communication.entity.NotificationHistory> page =
                historyService.getHistory(advocate, PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "sentAt")));
        byte[] csv = exportService.exportToCsv(page.getContent());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=notification_history.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    // ==================== LOGS ====================

    @GetMapping("/logs")
    public ResponseEntity<List<NotificationLog>> getLogs(
            @RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocate(token);
        return ResponseEntity.ok(logService.getLogs(advocate));
    }

    // ==================== QUEUE STATUS ====================

    @GetMapping("/queue/status")
    public ResponseEntity<Map<String, Object>> getQueueStatus(
            @RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocate(token);
        Map<String, Object> status = new HashMap<>();
        status.put("pending", queueRepository.countByAdvocateAndStatus(advocate, NotificationStatus.PENDING));
        status.put("processing", queueRepository.countByAdvocateAndStatus(advocate, NotificationStatus.PROCESSING));
        status.put("failed", queueRepository.countByAdvocateAndStatus(advocate, NotificationStatus.FAILED));
        status.put("failedPermanent", queueRepository.countByAdvocateAndStatus(advocate, NotificationStatus.FAILED_PERMANENTLY));
        return ResponseEntity.ok(status);
    }

    // ==================== TEST NOTIFICATION ====================

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testNotification(
            @RequestHeader("Authorization") String token,
            @RequestBody NotificationRequestDTO request) {
        Advocate advocate = getAdvocate(token);

        NotificationPayload payload = new NotificationPayload();
        payload.setRecipientName(request.getRecipientName());
        payload.setRecipientEmail(request.getRecipientEmail());
        payload.setRecipientPhone(request.getRecipientPhone());
        payload.setChannel(request.getChannel());
        payload.setType(request.getType() != null ? request.getType() : NotificationType.CUSTOM);
        payload.setSubject(request.getSubject());
        payload.setMessage(request.getMessage());
        payload.setVariables(request.getVariables());
        payload.setCaseId(request.getCaseId());
        payload.setClientId(request.getClientId());
        payload.setInvoiceId(request.getInvoiceId());
        payload.setAdvocateEmail(advocate.getEmail());

        CommunicationDispatcher.NotificationResult result = dispatcher.dispatch(payload, advocate);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("providerResponse", result.getProviderResponse());
        response.put("errorMessage", result.getErrorMessage());
        return ResponseEntity.ok(response);
    }

    // ==================== PRIVATE HELPERS ====================

    private LocalDateTime resolveDateRange(String range) {
        LocalDate today = LocalDate.now();
        return switch (range.toLowerCase()) {
            case "today" -> today.atStartOfDay();
            case "yesterday" -> today.minusDays(1).atStartOfDay();
            case "last7days" -> today.minusDays(7).atStartOfDay();
            case "last30days" -> today.minusDays(30).atStartOfDay();
            default -> null;
        };
    }
}
