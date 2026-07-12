package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.dto.NotificationHistoryDTO;
import advocate.com.advocate_app.communication.entity.NotificationHistory;
import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.communication.repository.CommunicationHistoryRepository;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunicationHistoryService {

    private final CommunicationHistoryRepository historyRepository;

    public CommunicationHistoryService(CommunicationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<NotificationHistoryDTO> getHistory(Advocate advocate) {
        return historyRepository.findByAdvocateOrderByCreatedAtDesc(advocate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<NotificationHistory> getHistory(Advocate advocate, Pageable pageable) {
        return historyRepository.findByAdvocateOrderBySentAtDesc(advocate, pageable);
    }

    public long getTotalCount(Advocate advocate) {
        return historyRepository.countByAdvocate(advocate);
    }

    public long getSentCount(Advocate advocate) {
        return historyRepository.countByAdvocateAndStatus(advocate, NotificationStatus.SENT);
    }

    public long getFailedCount(Advocate advocate) {
        return historyRepository.countByAdvocateAndStatus(advocate, NotificationStatus.FAILED);
    }

    public void recordSuccess(advocate.com.advocate_app.communication.dto.NotificationPayload payload,
                              String channel, String messageId) {
        // Deprecated — use CommunicationDispatcher.saveHistory instead
    }

    public void recordFailure(advocate.com.advocate_app.communication.dto.NotificationPayload payload,
                              String channel, String errorMessage) {
        // Deprecated — use CommunicationDispatcher.saveHistory instead
    }

    public void recordFailure(advocate.com.advocate_app.communication.dto.NotificationPayload payload,
                              String channel, String errorMessage, Integer statusCode, String responseBody) {
        // Deprecated — use CommunicationDispatcher.saveHistory instead
    }

    public void updateStatusByMessageId(String wamId, String newStatus) {
        Optional<NotificationHistory> opt = historyRepository.findByMetaMessageId(wamId);
        opt.ifPresent(h -> {
            h.setStatus(NotificationStatus.valueOf(newStatus));
            historyRepository.save(h);
        });
    }

    public void updateStatusByMessageId(String wamId, String newStatus, String errorMessage, Integer metaErrorCode) {
        Optional<NotificationHistory> opt = historyRepository.findByMetaMessageId(wamId);
        opt.ifPresent(h -> {
            h.setStatus(NotificationStatus.valueOf(newStatus));
            h.setErrorMessage(errorMessage);
            if (metaErrorCode != null) h.setStatusCode(metaErrorCode);
            historyRepository.save(h);
        });
    }

    public Page<NotificationHistory> filterHistory(Advocate advocate, String channel, String status,
                                                    String eventType, LocalDateTime from, LocalDateTime to,
                                                    String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return historyRepository.searchHistory(advocate, search, channel, status, eventType, from, to, pageable);
        }
        return historyRepository.filterHistory(advocate, channel, status, eventType, from, to, pageable);
    }

    public List<NotificationHistoryDTO> filterHistory(Advocate advocate, String channel, String status,
                                                       String eventType, LocalDateTime from, LocalDateTime to,
                                                       String search) {
        List<NotificationHistory> results;
        if (search != null && !search.isBlank()) {
            results = historyRepository.searchHistory(advocate, search, channel, status, eventType, from, to);
        } else {
            results = historyRepository.filterHistoryList(advocate, channel, status, eventType, from, to);
        }
        return results.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Map<String, Object> getStats(Advocate advocate) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.put("totalSent", historyRepository.countByAdvocateAndStatus(advocate, NotificationStatus.SENT));
        stats.put("failedTotal", historyRepository.countByAdvocateAndStatus(advocate, NotificationStatus.FAILED));
        stats.put("pendingTotal", historyRepository.countByAdvocateAndStatus(advocate, NotificationStatus.PENDING));
        stats.put("emailsToday", historyRepository.countByAdvocateAndChannelSince(advocate, NotificationChannel.EMAIL, todayStart));
        stats.put("whatsappToday", historyRepository.countByAdvocateAndChannelSince(advocate, NotificationChannel.WHATSAPP, todayStart));
        stats.put("failedToday", historyRepository.countFailedSince(advocate, todayStart));
        stats.put("sentToday", historyRepository.countByAdvocateAndStatusSince(advocate, NotificationStatus.SENT, todayStart));
        return stats;
    }

    private NotificationHistoryDTO toDTO(NotificationHistory entity) {
        NotificationHistoryDTO dto = new NotificationHistoryDTO();
        dto.setId(entity.getId());
        dto.setChannel(entity.getChannel());
        dto.setType(entity.getType());
        dto.setRecipient(entity.getRecipient());
        dto.setSubject(entity.getSubject());
        dto.setMessage(entity.getMessage());
        dto.setStatus(entity.getStatus());
        dto.setProviderResponse(entity.getProviderResponse());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setSentAt(entity.getSentAt());
        return dto;
    }
}
