package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_otp", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_email_created", columnList = "email, created_at")
})
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long advocateId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 128)
    private String hashedOtp;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdvocateId() { return advocateId; }
    public void setAdvocateId(Long advocateId) { this.advocateId = advocateId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHashedOtp() { return hashedOtp; }
    public void setHashedOtp(String hashedOtp) { this.hashedOtp = hashedOtp; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
