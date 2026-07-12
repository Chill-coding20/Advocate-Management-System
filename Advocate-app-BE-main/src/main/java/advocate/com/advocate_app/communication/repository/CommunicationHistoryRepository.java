package advocate.com.advocate_app.communication.repository;

import advocate.com.advocate_app.communication.entity.NotificationHistory;
import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommunicationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    List<NotificationHistory> findByAdvocateOrderByCreatedAtDesc(Advocate advocate);

    List<NotificationHistory> findByAdvocateOrderBySentAtDesc(Advocate advocate);

    Page<NotificationHistory> findByAdvocateOrderBySentAtDesc(Advocate advocate, Pageable pageable);

    List<NotificationHistory> findByAdvocateAndCreatedAtAfter(Advocate advocate, LocalDateTime after);

    List<NotificationHistory> findByAdvocateAndChannelOrderBySentAtDesc(Advocate advocate, String channel);

    List<NotificationHistory> findByAdvocateAndStatusOrderBySentAtDesc(Advocate advocate, String status);

    List<NotificationHistory> findByAdvocateAndEventTypeOrderBySentAtDesc(Advocate advocate, String eventType);

    long countByAdvocate(Advocate advocate);

    long countByAdvocateAndStatus(Advocate advocate, NotificationStatus status);

    long countByAdvocateAndChannel(Advocate advocate, String channel);

    @Query("SELECT COUNT(n) FROM NotificationHistory n WHERE n.advocate = :advocate AND n.channel = :channel AND n.sentAt >= :since")
    long countByAdvocateAndChannelSince(@Param("advocate") Advocate advocate, @Param("channel") NotificationChannel channel, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(n) FROM NotificationHistory n WHERE n.advocate = :advocate AND n.status = :status AND n.sentAt >= :since")
    long countByAdvocateAndStatusSince(@Param("advocate") Advocate advocate, @Param("status") NotificationStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(n) FROM NotificationHistory n WHERE n.advocate = :advocate AND n.status = 'FAILED' AND n.sentAt >= :since")
    long countFailedSince(@Param("advocate") Advocate advocate, @Param("since") LocalDateTime since);

    Optional<NotificationHistory> findByMetaMessageId(String metaMessageId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationHistory n SET n.status = :status, n.errorMessage = :errorMessage WHERE n.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("errorMessage") String errorMessage);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationHistory n SET n.status = :status, n.errorMessage = :errorMessage, n.retryCount = COALESCE(n.retryCount, 0) + 1 WHERE n.id = :id")
    int updateStatusFailed(@Param("id") Long id, @Param("status") String status, @Param("errorMessage") String errorMessage);

    @Query("SELECT n FROM NotificationHistory n WHERE n.advocate = :advocate " +
           "AND (:channel IS NULL OR n.channel = :channel) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:eventType IS NULL OR n.eventType = :eventType) " +
           "AND (:from IS NULL OR n.sentAt >= :from) " +
           "AND (:to IS NULL OR n.sentAt <= :to)")
    Page<NotificationHistory> filterHistory(@Param("advocate") Advocate advocate,
                                             @Param("channel") String channel,
                                             @Param("status") String status,
                                             @Param("eventType") String eventType,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             Pageable pageable);

    @Query("SELECT n FROM NotificationHistory n WHERE n.advocate = :advocate " +
           "AND (:channel IS NULL OR n.channel = :channel) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:eventType IS NULL OR n.eventType = :eventType) " +
           "AND (:from IS NULL OR n.sentAt >= :from) " +
           "AND (:to IS NULL OR n.sentAt <= :to) " +
           "ORDER BY n.sentAt DESC")
    List<NotificationHistory> filterHistoryList(@Param("advocate") Advocate advocate,
                                                 @Param("channel") String channel,
                                                 @Param("status") String status,
                                                 @Param("eventType") String eventType,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);

    @Query("SELECT n FROM NotificationHistory n WHERE n.advocate = :advocate " +
           "AND (:search IS NULL OR n.recipient LIKE %:search% OR n.subject LIKE %:search% " +
           "OR n.recipientName LIKE %:search% OR n.recipientEmail LIKE %:search% " +
           "OR n.recipientPhone LIKE %:search%) " +
           "AND (:channel IS NULL OR n.channel = :channel) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:eventType IS NULL OR n.eventType = :eventType) " +
           "AND (:from IS NULL OR n.sentAt >= :from) " +
           "AND (:to IS NULL OR n.sentAt <= :to)")
    Page<NotificationHistory> searchHistory(@Param("advocate") Advocate advocate,
                                             @Param("search") String search,
                                             @Param("channel") String channel,
                                             @Param("status") String status,
                                             @Param("eventType") String eventType,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             Pageable pageable);

    @Query("SELECT n FROM NotificationHistory n WHERE n.advocate = :advocate " +
           "AND (:search IS NULL OR n.recipient LIKE %:search% OR n.subject LIKE %:search% " +
           "OR n.recipientName LIKE %:search% OR n.recipientEmail LIKE %:search% " +
           "OR n.recipientPhone LIKE %:search%) " +
           "AND (:channel IS NULL OR n.channel = :channel) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:eventType IS NULL OR n.eventType = :eventType) " +
           "AND (:from IS NULL OR n.sentAt >= :from) " +
           "AND (:to IS NULL OR n.sentAt <= :to) " +
           "ORDER BY n.sentAt DESC")
    List<NotificationHistory> searchHistory(@Param("advocate") Advocate advocate,
                                             @Param("search") String search,
                                             @Param("channel") String channel,
                                             @Param("status") String status,
                                             @Param("eventType") String eventType,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);
}
