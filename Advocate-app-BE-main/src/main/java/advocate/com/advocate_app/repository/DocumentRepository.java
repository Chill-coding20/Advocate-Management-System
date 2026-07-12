package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByAdvocateOrderByUploadDateDesc(Advocate advocate);
    List<Document> findByAdvocate(Advocate advocate);
    Page<Document> findByAdvocate(Advocate advocate, Pageable pageable);
    List<Document> findByAdvocateAndCaseEntity(Advocate advocate, CaseEntity caseEntity);
    List<Document> findByAdvocateAndClient(Advocate advocate, Client client);

    Optional<Document> findTopByOriginalNameAndAdvocateOrderByVersionDesc(String originalName, Advocate advocate);

    long countByAdvocateAndCaseEntity(Advocate advocate, CaseEntity caseEntity);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.advocate = :advocate")
    long countByAdvocate(@Param("advocate") Advocate advocate);

    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d WHERE d.advocate = :advocate")
    long sumFileSizeByAdvocate(@Param("advocate") Advocate advocate);

    @Query("SELECT d.category, COUNT(d) FROM Document d WHERE d.advocate = :advocate AND d.category IS NOT NULL GROUP BY d.category")
    List<Object[]> countByCategoryGrouped(@Param("advocate") Advocate advocate);

    @Query("SELECT CAST(d.uploadDate AS date), COUNT(d) FROM Document d WHERE d.advocate = :advocate AND d.uploadDate >= :since GROUP BY CAST(d.uploadDate AS date) ORDER BY CAST(d.uploadDate AS date) ASC")
    List<Object[]> uploadActivitySince(@Param("advocate") Advocate advocate, @Param("since") java.time.LocalDateTime since);

    @Query("SELECT d FROM Document d WHERE d.advocate = :advocate AND " +
           "(:keyword IS NULL OR " +
           "LOWER(d.documentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.originalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.uploadDate DESC")
    List<Document> globalSearch(@Param("advocate") Advocate advocate, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.advocate = :advocate AND " +
           "(:keyword IS NULL OR " +
           "LOWER(d.documentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.originalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.uploadDate DESC")
    List<Document> searchDocuments(@Param("advocate") Advocate advocate, @Param("keyword") String keyword);

    @Query("SELECT d FROM Document d WHERE d.advocate = :advocate AND " +
           "(:category IS NULL OR d.category = :category) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:fileType IS NULL OR d.fileType LIKE CONCAT(:fileType, '%')) " +
           "ORDER BY d.uploadDate DESC")
    List<Document> filterDocuments(@Param("advocate") Advocate advocate,
                                   @Param("category") String category,
                                   @Param("status") String status,
                                   @Param("fileType") String fileType);

    @Query("SELECT d FROM Document d WHERE d.advocate = :advocate AND " +
           "(:keyword IS NULL OR " +
           "LOWER(d.documentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.originalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR d.category = :category) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:fileType IS NULL OR d.fileType LIKE CONCAT(:fileType, '%')) " +
           "ORDER BY d.uploadDate DESC")
    Page<Document> searchAndFilterDocuments(@Param("advocate") Advocate advocate,
                                            @Param("keyword") String keyword,
                                            @Param("category") String category,
                                            @Param("status") String status,
                                            @Param("fileType") String fileType,
                                            Pageable pageable);
}
