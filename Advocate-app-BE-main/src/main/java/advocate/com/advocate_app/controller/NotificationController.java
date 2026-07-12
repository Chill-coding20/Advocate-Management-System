package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.NotificationDTO;
import advocate.com.advocate_app.dto.NotificationHistoryDTO;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.NotificationEntity;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.NotificationMapper;
import advocate.com.advocate_app.communication.entity.NotificationHistory;
import advocate.com.advocate_app.communication.service.CommunicationHistoryService;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.NotificationScheduler;
import advocate.com.advocate_app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationScheduler notificationScheduler;

    @Autowired
    private CommunicationHistoryService historyService;

    // ==================== IN-APP NOTIFICATIONS (preserved) ====================

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotificationsPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = getAdvocate(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationEntity> notifPage = notificationService.getNotificationsPaged(advocate, pageable);
        return ResponseEntity.ok(notifPage.map(notificationMapper::toDTO));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = getAdvocate(email);
        List<NotificationEntity> unread = notificationService.getUnreadNotifications(advocate);
        List<NotificationDTO> dtos = unread.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<String> markAsRead(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        notificationService.markAsRead(id, email);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PostMapping("/trigger-check")
    public ResponseEntity<String> triggerCheck(
            @RequestHeader("Authorization") String token) {
        getAdvocate(JwtUtil.extractEmail(token.substring(7)));
        notificationScheduler.generateDailyNotifications();
        return ResponseEntity.ok("Notification check triggered successfully.");
    }

    // ==================== NOTIFICATION HISTORY ====================

    /**
     * GET /api/notifications/history
     * Returns paginated notification history for the logged-in advocate.
     */
    @GetMapping("/history")
    public ResponseEntity<Page<NotificationHistoryDTO>> getHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Advocate advocate = getAdvocate(JwtUtil.extractEmail(token.substring(7)));
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<NotificationHistory> historyPage = historyService.getHistory(advocate, pageable);
        return ResponseEntity.ok(historyPage.map(this::toHistoryDTO));
    }

    /**
     * GET /api/notifications/history/filter
     * Returns filtered + paginated notification history.
     * Optional params: channel, status, eventType, from (ISO datetime), to (ISO datetime)
     */
    @GetMapping("/history/filter")
    public ResponseEntity<Page<NotificationHistoryDTO>> filterHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Advocate advocate = getAdvocate(JwtUtil.extractEmail(token.substring(7)));
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<NotificationHistory> result = historyService.filterHistory(
                advocate, channel, status, eventType, from, to, null, pageable);
        return ResponseEntity.ok(result.map(this::toHistoryDTO));
    }

    /**
     * GET /api/notifications/history/stats
     * Returns dashboard statistics for the Notification Center.
     */
    @GetMapping("/history/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocate(JwtUtil.extractEmail(token.substring(7)));
        return ResponseEntity.ok(historyService.getStats(advocate));
    }

    // ==================== PRIVATE HELPERS ====================

    private Advocate getAdvocate(String email) {
        return advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
    }

    private NotificationHistoryDTO toHistoryDTO(NotificationHistory h) {
        NotificationHistoryDTO dto = new NotificationHistoryDTO();
        dto.setId(h.getId());
        dto.setEventType(h.getEventType());
        dto.setChannel(h.getChannel() != null ? h.getChannel().name() : null);
        dto.setStatus(h.getStatus() != null ? h.getStatus().name() : null);
        dto.setRecipientName(h.getRecipientName());
        dto.setRecipientEmail(h.getRecipientEmail());
        dto.setRecipientPhone(h.getRecipientPhone());
        dto.setSubject(h.getSubject());
        dto.setBody(h.getBody());
        dto.setErrorMessage(h.getErrorMessage());
        dto.setStatusCode(h.getStatusCode());
        dto.setResponseBody(h.getResponseBody());
        dto.setMetaMessageId(h.getMetaMessageId());
        dto.setSentAt(h.getSentAt());
        if (h.getCaseEntity() != null) {
            dto.setCaseId(h.getCaseEntity().getId());
            dto.setCaseNumber(h.getCaseEntity().getCaseNumber());
        }
        if (h.getClient() != null) {
            dto.setClientId(h.getClient().getId());
            dto.setClientName(h.getClient().getName());
        }
        return dto;
    }
}
