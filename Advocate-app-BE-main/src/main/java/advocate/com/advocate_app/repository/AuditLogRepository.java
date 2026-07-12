package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByAdvocateIdOrderByCreatedAtDesc(Long advocateId, Pageable pageable);

    Page<AuditLog> findByAdvocateIdAndActionTypeInOrderByCreatedAtDesc(
            Long advocateId, List<String> actionTypes, Pageable pageable);

    Page<AuditLog> findByAdvocateIdAndModuleOrderByCreatedAtDesc(
            Long advocateId, String module, Pageable pageable);

    Page<AuditLog> findByAdvocateIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long advocateId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByAdvocateIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            Long advocateId, String searchText, Pageable pageable);

    Page<AuditLog> findByAdvocateIdAndStatusOrderByCreatedAtDesc(
            Long advocateId, String status, Pageable pageable);
}
