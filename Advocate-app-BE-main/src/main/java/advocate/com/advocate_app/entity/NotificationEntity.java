package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_advocate", columnList = "advocate_id"),
    @Index(name = "idx_notif_advocate_read_created", columnList = "advocate_id, read_status, created_at")
})
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean readStatus = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advocate_id", nullable = false)
    private Advocate advocate;

    // --- Constructors ---
    public NotificationEntity() {}

    public NotificationEntity(String message, Advocate advocate) {
        this.message = message;
        this.advocate = advocate;
        this.createdAt = LocalDateTime.now();
        this.readStatus = false;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}
