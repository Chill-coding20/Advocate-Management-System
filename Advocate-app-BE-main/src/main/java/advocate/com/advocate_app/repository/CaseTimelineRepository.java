package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.CaseTimelineEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CaseTimelineRepository extends JpaRepository<CaseTimelineEvent, Long> {

    void deleteByCaseId(Long caseId);

    List<CaseTimelineEvent> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    Page<CaseTimelineEvent> findByCaseIdOrderByCreatedAtDesc(Long caseId, Pageable pageable);

    Page<CaseTimelineEvent> findByCaseIdAndEventTypeInOrderByCreatedAtDesc(
            Long caseId, List<String> eventTypes, Pageable pageable);

    Page<CaseTimelineEvent> findByCaseIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long caseId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<CaseTimelineEvent> findByCaseIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            Long caseId, String searchText, Pageable pageable);
}
