package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.NotificationEntity;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByAdvocateAndReadStatusFalseOrderByCreatedAtDesc(Advocate advocate);

    Page<NotificationEntity> findByAdvocate(Advocate advocate, Pageable pageable);

    long countByAdvocate(Advocate advocate);
}
