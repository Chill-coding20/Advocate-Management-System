package advocate.com.advocate_app.communication.repository;

import advocate.com.advocate_app.communication.entity.NotificationQueue;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, Long> {

    List<NotificationQueue> findByStatusAndNextRetryAtBeforeOrNextRetryAtIsNullOrderByCreatedAtAsc(
            NotificationStatus status, LocalDateTime now);

    @Query("SELECT q FROM NotificationQueue q WHERE q.status = :status AND (q.nextRetryAt IS NULL OR q.nextRetryAt <= :now) ORDER BY q.createdAt ASC")
    List<NotificationQueue> findReadyToProcess(@Param("status") NotificationStatus status, @Param("now") LocalDateTime now);

    long countByStatus(NotificationStatus status);

    long countByAdvocateAndStatus(Advocate advocate, NotificationStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationQueue q SET q.status = :status, q.processingStartedAt = :now WHERE q.id = :id AND q.status = 'PENDING'")
    int lockForProcessing(@Param("id") Long id, @Param("status") NotificationStatus status, @Param("now") LocalDateTime now);

    Optional<NotificationQueue> findByIdAndAdvocate(Long id, Advocate advocate);
}
