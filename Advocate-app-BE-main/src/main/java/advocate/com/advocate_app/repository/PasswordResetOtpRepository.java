package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByEmailOrderByCreatedAtDesc(String email);
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime after);
    void deleteByEmail(String email);
}
