package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.BackupHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long> {
    List<BackupHistory> findByAdvocateOrderByCreatedAtDesc(Advocate advocate);
    void deleteByAdvocateAndId(Advocate advocate, Long id);
}
