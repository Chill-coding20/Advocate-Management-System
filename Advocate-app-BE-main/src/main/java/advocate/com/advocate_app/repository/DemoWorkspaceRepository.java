package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.DemoWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DemoWorkspaceRepository extends JpaRepository<DemoWorkspace, Long> {
    Optional<DemoWorkspace> findByAdvocateId(Long advocateId);
    boolean existsByAdvocateId(Long advocateId);
    void deleteByAdvocateId(Long advocateId);
}
