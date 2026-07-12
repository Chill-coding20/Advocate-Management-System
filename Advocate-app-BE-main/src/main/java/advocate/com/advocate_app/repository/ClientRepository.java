package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    // ✅ Fetch only non-deleted clients
    @Query("SELECT c FROM Client c WHERE c.deleted = false")
    List<Client> findAllActive();

    // ✅ Fetch only deleted clients
    @Query("SELECT c FROM Client c WHERE c.deleted = true")
    List<Client> findAllArchived();

    // ✅ Search non-deleted clients
    @Query("SELECT c FROM Client c WHERE c.deleted = false AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Client> searchActiveClients(String keyword);

    // ✅ Fetch active clients by advocate
    @Query("SELECT c FROM Client c WHERE c.deleted = false AND c.advocate = :advocate")
    List<Client> findAllActiveByAdvocate(@Param("advocate") Advocate advocate);

    Page<Client> findByAdvocateAndDeletedFalse(Advocate advocate, Pageable pageable);

    Page<Client> findByAdvocateAndDeletedTrue(Advocate advocate, Pageable pageable);

    @Query("SELECT c FROM Client c WHERE c.deleted = false AND c.advocate = :advocate AND c.createdAt BETWEEN :start AND :end")
    List<Client> findAllActiveByAdvocateAndCreatedAtBetween(@Param("advocate") Advocate advocate, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // ✅ Fetch archived clients by advocate
    @Query("SELECT c FROM Client c WHERE c.deleted = true AND c.advocate = :advocate")
    List<Client> findAllArchivedByAdvocate(@Param("advocate") Advocate advocate);

    // ✅ Search active clients by advocate
    @Query("SELECT c FROM Client c WHERE c.deleted = false AND c.advocate = :advocate AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Client> searchActiveClientsByAdvocate(@Param("advocate") Advocate advocate, @Param("keyword") String keyword);

    long countByAdvocate(Advocate advocate);

    long countByAdvocateAndDeletedFalse(Advocate advocate);

    List<Client> findByAdvocateIsNull();

    @Query("SELECT c FROM Client c WHERE c.deleted = false AND c.advocate = :advocate AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Client> globalSearch(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Client c WHERE c.deleted = false AND c.advocate = :advocate AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Client> searchActiveClientsByAdvocatePaged(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Client c WHERE c.deleted = true AND c.advocate = :advocate AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Client> searchArchivedClientsByAdvocatePaged(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);
}

