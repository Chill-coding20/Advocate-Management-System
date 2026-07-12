package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAdvocate(Advocate advocate);

    Page<Task> findByAdvocate(Advocate advocate, Pageable pageable);

    List<Task> findByAdvocateOrderByCompletedAscDeadlineAsc(Advocate advocate);

    long countByAdvocate(Advocate advocate);

    @Query("SELECT t FROM Task t WHERE t.advocate = :advocate AND t.completed = false " +
           "AND t.deadline IS NOT NULL AND t.deadline BETWEEN :from AND :to")
    List<Task> findUpcomingIncompleteTasksForAdvocate(@Param("advocate") Advocate advocate,
                                                       @Param("from") LocalDate from,
                                                       @Param("to") LocalDate to);

    @Query("SELECT t FROM Task t WHERE t.advocate = :advocate AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> globalSearch(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);
}
