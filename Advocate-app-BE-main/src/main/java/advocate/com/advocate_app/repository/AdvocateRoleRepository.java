package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.AdvocateRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AdvocateRoleRepository extends JpaRepository<AdvocateRole, Long> {
    List<AdvocateRole> findByAdvocateId(Long advocateId);
    Optional<AdvocateRole> findByAdvocateIdAndRoleId(Long advocateId, Long roleId);
    boolean existsByAdvocateIdAndRoleId(Long advocateId, Long roleId);
    void deleteByAdvocateId(Long advocateId);
    void deleteByAdvocateIdAndRoleId(Long advocateId, Long roleId);
}
