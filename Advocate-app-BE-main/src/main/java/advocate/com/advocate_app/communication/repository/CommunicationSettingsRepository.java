package advocate.com.advocate_app.communication.repository;

import advocate.com.advocate_app.communication.entity.CommunicationSettings;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunicationSettingsRepository extends JpaRepository<CommunicationSettings, Long> {
    Optional<CommunicationSettings> findByAdvocate(Advocate advocate);
}
