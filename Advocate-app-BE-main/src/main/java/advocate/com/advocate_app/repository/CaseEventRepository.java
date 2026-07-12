package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CaseEventRepository extends JpaRepository<CaseEventEntity, Long> {

    // ✅ All events of a specific advocate
    List<CaseEventEntity> findByAdvocate(Advocate advocate);

    Page<CaseEventEntity> findByAdvocate(Advocate advocate, Pageable pageable);

    List<CaseEventEntity> findByCaseEntityAndAdvocate(CaseEntity caseEntity, Advocate advocate);

    // ✅ Events for a specific date (e.g., today)
    @Query("SELECT e FROM CaseEventEntity e WHERE e.advocate = :advocate AND e.date = :date")
    List<CaseEventEntity> findByAdvocateAndDate(@Param("advocate") Advocate advocate,
                                                @Param("date") LocalDate date);

    // ✅ Upcoming events within a date range (used in dashboard or reminders)
    @Query("SELECT e FROM CaseEventEntity e WHERE e.advocate = :advocate AND e.date BETWEEN :startDate AND :endDate ORDER BY e.date ASC")
    List<CaseEventEntity> findUpcomingEvents(@Param("advocate") Advocate advocate,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // ✅ NEW — Fetch all events happening today or tomorrow (used by NotificationScheduler)
    @Query("SELECT e FROM CaseEventEntity e WHERE e.date BETWEEN :today AND :tomorrow")
    List<CaseEventEntity> findEventsForTodayAndTomorrow(@Param("today") LocalDate today,
                                                        @Param("tomorrow") LocalDate tomorrow);

    @Query("SELECT e FROM CaseEventEntity e WHERE e.advocate = :advocate AND e.date BETWEEN :start AND :end ORDER BY e.date ASC")
    List<CaseEventEntity> findByAdvocateAndDateBetween(@Param("advocate") Advocate advocate,
                                                        @Param("start") LocalDate start,
                                                        @Param("end") LocalDate end);

    @Query("SELECT COUNT(e) FROM CaseEventEntity e WHERE e.advocate = :advocate AND e.date BETWEEN :start AND :end")
    long countUpcomingForAdvocate(@Param("advocate") Advocate advocate,
                                  @Param("start") LocalDate start,
                                  @Param("end") LocalDate end);

    long countByAdvocate(Advocate advocate);

    @Modifying
    @Query("DELETE FROM CaseEventEntity e WHERE e.date < :cutoff")
    void deleteByDateBefore(@Param("cutoff") LocalDate cutoff);

    @Query("SELECT e FROM CaseEventEntity e WHERE e.advocate = :advocate AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<CaseEventEntity> globalSearch(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);
}
