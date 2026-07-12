package advocate.com.advocate_app.communication.repository;

import advocate.com.advocate_app.communication.entity.NotificationTemplate;
import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    List<NotificationTemplate> findByAdvocateOrderByCreatedAtDesc(Advocate advocate);
    Optional<NotificationTemplate> findByIdAndAdvocate(Long id, Advocate advocate);
    Optional<NotificationTemplate> findByAdvocateAndChannelAndTypeAndActiveTrue(Advocate advocate, NotificationChannel channel, NotificationType type);
}
