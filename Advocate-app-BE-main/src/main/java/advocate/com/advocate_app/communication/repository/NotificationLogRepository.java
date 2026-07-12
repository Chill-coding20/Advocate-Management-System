package advocate.com.advocate_app.communication.repository;

import advocate.com.advocate_app.communication.entity.NotificationLog;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByAdvocateOrderByCreatedAtDesc(Advocate advocate);
}
