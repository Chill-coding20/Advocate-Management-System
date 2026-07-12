package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.entity.NotificationLog;
import advocate.com.advocate_app.communication.repository.NotificationLogRepository;
import advocate.com.advocate_app.entity.Advocate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationLogService {

    private static final Logger log = LoggerFactory.getLogger(NotificationLogService.class);

    private final NotificationLogRepository logRepository;

    public NotificationLogService(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void log(Advocate advocate, String recipient, String eventType,
                    String channel, String level, String message, String details) {
        try {
            NotificationLog entry = new NotificationLog();
            entry.setAdvocate(advocate);
            entry.setRecipient(recipient);
            entry.setEventType(eventType);
            entry.setChannel(channel);
            entry.setLogLevel(level);
            entry.setMessage(message);
            entry.setDetails(details);
            entry.setCreatedAt(LocalDateTime.now());
            logRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist communication log: {}", e.getMessage());
        }
    }

    public List<NotificationLog> getLogs(Advocate advocate) {
        return logRepository.findByAdvocateOrderByCreatedAtDesc(advocate);
    }
}
