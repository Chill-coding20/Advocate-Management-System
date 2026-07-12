package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClientPaymentRepository extends JpaRepository<ClientPayment, Long> {
    long countByAdvocate(Advocate advocate);
    List<ClientPayment> findByAdvocate(Advocate advocate);

    Page<ClientPayment> findByAdvocate(Advocate advocate, Pageable pageable);
    List<ClientPayment> findByCaseEntity(CaseEntity caseEntity);
    List<ClientPayment> findByClient(Client client);
    List<ClientPayment> findByPaymentDateBetween(java.util.Date start, java.util.Date end);
    List<ClientPayment> findByCaseEntityAndAdvocate(CaseEntity caseEntity, Advocate advocate);
    List<ClientPayment> findByAdvocateAndPaymentDateBetween(Advocate advocate, java.util.Date start, java.util.Date end);
    List<ClientPayment> findByClientAndAdvocate(Client client, Advocate advocate);

    @Query("SELECT SUM(p.amount) FROM ClientPayment p WHERE p.advocate = :advocate AND p.paymentDate BETWEEN :start AND :end")
    Double sumByAdvocateAndDateBetween(@Param("advocate") Advocate advocate,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);

    @Query("SELECT p FROM ClientPayment p WHERE p.advocate = :advocate AND " +
           "(LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.paymentMode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.referenceNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ClientPayment> globalSearch(@Param("advocate") Advocate advocate,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);
}
