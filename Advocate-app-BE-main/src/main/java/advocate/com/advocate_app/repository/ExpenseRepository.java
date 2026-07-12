package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ✅ Fetch all expenses for a specific advocate
    List<Expense> findByAdvocate(Advocate advocate);

    Page<Expense> findByAdvocate(Advocate advocate, Pageable pageable);

    // ✅ Fetch all expenses for a specific case
    List<Expense> findByCaseEntity(CaseEntity caseEntity);

    List<Expense> findByCaseEntityAndAdvocate(CaseEntity caseEntity, Advocate advocate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.advocate = :advocate AND e.paymentDate BETWEEN :start AND :end")
    Double sumByAdvocateAndDateBetween(@Param("advocate") Advocate advocate,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    @Query("SELECT e FROM Expense e WHERE e.advocate = :advocate AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Expense> globalSearch(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);

    long countByAdvocate(Advocate advocate);
}
