package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByAdvocate(Advocate advocate);

    Page<Invoice> findByAdvocate(Advocate advocate, Pageable pageable);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByAdvocateAndStatus(Advocate advocate, String status);

    List<Invoice> findByCaseEntityAndAdvocate(CaseEntity caseEntity, Advocate advocate);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.advocate = :advocate AND i.status = :status")
    long countByAdvocateAndStatus(@Param("advocate") Advocate advocate, @Param("status") String status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.advocate = :advocate AND i.status = :status AND i.invoiceDate BETWEEN :start AND :end")
    long countByAdvocateAndStatusBetween(@Param("advocate") Advocate advocate, @Param("status") String status, @Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    long countByAdvocate(Advocate advocate);

    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.advocate = :advocate AND i.status = :status")
    Double sumAmountByAdvocateAndStatus(@Param("advocate") Advocate advocate, @Param("status") String status);

    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.advocate = :advocate")
    Double sumTotalAmountByAdvocate(@Param("advocate") Advocate advocate);

    @Query("SELECT i FROM Invoice i WHERE i.advocate = :advocate AND " +
           "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.status) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Invoice> globalSearch(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);
}
