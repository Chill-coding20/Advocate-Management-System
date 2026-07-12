package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<CaseEntity, Long> {

    List<CaseEntity> findByAdvocate(Advocate advocate);

    Page<CaseEntity> findByAdvocate(Advocate advocate, Pageable pageable);

    Page<CaseEntity> findByAdvocateAndDeletedFalse(Advocate advocate, Pageable pageable);

    Page<CaseEntity> findByAdvocateAndDeletedTrue(Advocate advocate, Pageable pageable);

    List<CaseEntity> findByAdvocateAndDeletedFalse(Advocate advocate);

    Optional<CaseEntity> findByCaseNumber(String caseNumber);

    boolean existsByCaseNumber(String caseNumber);

    long countByAdvocateAndDeletedFalse(Advocate advocate);

    long countByAdvocateAndStatusAndDeletedFalse(Advocate advocate, String status);

    @Query("SELECT c.status, COUNT(c) FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false GROUP BY c.status")
    List<Object[]> countByStatusGrouped(@Param("advocate") Advocate advocate);

    @Query("SELECT c.status, COUNT(c) FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false AND c.createdAt BETWEEN :start AND :end GROUP BY c.status")
    List<Object[]> countByStatusGroupedBetween(@Param("advocate") Advocate advocate, @Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT c.courtLevel, c.status, COUNT(c) FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false AND c.courtLevel IS NOT NULL GROUP BY c.courtLevel, c.status")
    List<Object[]> countByCourtAndStatusGrouped(@Param("advocate") Advocate advocate);

    @Query("SELECT c.courtLevel, c.status, COUNT(c) FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false AND c.courtLevel IS NOT NULL AND c.createdAt BETWEEN :start AND :end GROUP BY c.courtLevel, c.status")
    List<Object[]> countByCourtAndStatusGroupedBetween(@Param("advocate") Advocate advocate, @Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT MONTH(c.createdAt), " +
           "SUM(CASE WHEN c.status = 'Active' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.status = 'Closed' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.status = 'Pending' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.status = 'Dismissed' THEN 1 ELSE 0 END) " +
           "FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false AND c.createdAt IS NOT NULL " +
           "GROUP BY MONTH(c.createdAt) ORDER BY MONTH(c.createdAt)")
    List<Object[]> monthlyCaseStatus(@Param("advocate") Advocate advocate);

    @Query("SELECT MONTH(c.createdAt), " +
           "SUM(CASE WHEN c.status = 'Active' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.status = 'Closed' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.status = 'Pending' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.status = 'Dismissed' THEN 1 ELSE 0 END) " +
           "FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false AND c.createdAt BETWEEN :start AND :end " +
           "GROUP BY MONTH(c.createdAt) ORDER BY MONTH(c.createdAt)")
    List<Object[]> monthlyCaseStatusBetween(@Param("advocate") Advocate advocate, @Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COUNT(c) FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false AND c.createdAt BETWEEN :start AND :end")
    long countByAdvocateAndDeletedFalseBetween(@Param("advocate") Advocate advocate, @Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT COUNT(c) FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false AND c.status = :status AND c.createdAt BETWEEN :start AND :end")
    long countByAdvocateAndStatusAndDeletedFalseBetween(@Param("advocate") Advocate advocate, @Param("status") String status, @Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT c FROM CaseEntity c LEFT JOIN c.client cl " +
            "WHERE c.advocate = :advocate AND (:keyword IS NULL OR " +
            "LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.caseTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.caseType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.courtLevel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
    List<CaseEntity> searchCases(@Param("keyword") String keyword, @Param("advocate") Advocate advocate);

    @Query(value = "SELECT c FROM CaseEntity c LEFT JOIN c.client cl " +
            "WHERE c.advocate = :advocate AND (:keyword IS NULL OR " +
            "LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.caseTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.caseType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.courtLevel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
    Page<CaseEntity> searchCasesPaged(@Param("keyword") String keyword, @Param("advocate") Advocate advocate, Pageable pageable);

    @Query(value = "SELECT c FROM CaseEntity c LEFT JOIN c.client cl " +
            "WHERE c.advocate = :advocate AND c.deleted = true AND (:keyword IS NULL OR " +
            "LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.caseTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.caseType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.courtLevel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cl.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
    Page<CaseEntity> searchArchivedCasesPaged(@Param("keyword") String keyword, @Param("advocate") Advocate advocate, Pageable pageable);

    @Query("SELECT c.caseType, COUNT(c) FROM CaseEntity c WHERE c.advocate = :advocate AND c.deleted = false AND c.caseType IS NOT NULL AND c.createdAt BETWEEN :start AND :end GROUP BY c.caseType")
    List<Object[]> countByTypeGroupedBetween(@Param("advocate") Advocate advocate, @Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    @Query("SELECT c FROM CaseEntity c WHERE c.advocate = :advocate AND " +
           "(LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.caseTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.caseType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.courtLevel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<CaseEntity> globalSearch(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);
}
