package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdvocateRepository extends JpaRepository<Advocate, Long> {
    Optional<Advocate> findByEmail(String email);
}
