package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByAdvocateOrderByTimestampDesc(Advocate advocate);

    Page<Activity> findByAdvocate(Advocate advocate, Pageable pageable);

    long countByAdvocate(Advocate advocate);
}
