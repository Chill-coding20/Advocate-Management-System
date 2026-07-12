package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.NotificationEntity;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AdvocateRepository advocateRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                AdvocateRepository advocateRepository) {
        this.notificationRepository = notificationRepository;
        this.advocateRepository = advocateRepository;
    }

    // ✅ Create new notification
    public void createNotification(String message, Advocate advocate) {
        NotificationEntity notification = new NotificationEntity(message, advocate);
        notificationRepository.save(notification);
    }

    // ✅ Get all notifications for an advocate (paginated)
    public Page<NotificationEntity> getNotificationsPaged(Advocate advocate, Pageable pageable) {
        return notificationRepository.findByAdvocate(advocate, pageable);
    }

    // ✅ Get unread notifications for an advocate
    public List<NotificationEntity> getUnreadNotifications(Advocate advocate) {
        return notificationRepository.findByAdvocateAndReadStatusFalseOrderByCreatedAtDesc(advocate);
    }

    // ✅ Mark notification as read
    public void markAsRead(Long notificationId, String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (!notification.getAdvocate().getId().equals(advocate.getId())) {
                throw new RuntimeException("Unauthorized to modify this notification");
            }
            notification.setReadStatus(true);
            notificationRepository.save(notification);
        });
    }
}
