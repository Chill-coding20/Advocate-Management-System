package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "advocate_roles", indexes = {
    @Index(name = "idx_advrole_advocate", columnList = "advocate_id"),
    @Index(name = "idx_advrole_role", columnList = "role_id"),
    @Index(name = "uq_advrole_advocate_role", columnList = "advocate_id, role_id", unique = true)
})
public class AdvocateRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advocate_id", nullable = false)
    private Long advocateId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public AdvocateRole() {}

    public AdvocateRole(Long advocateId, Long roleId) {
        this.advocateId = advocateId;
        this.roleId = roleId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdvocateId() { return advocateId; }
    public void setAdvocateId(Long advocateId) { this.advocateId = advocateId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
