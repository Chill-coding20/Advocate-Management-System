package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_permissions", indexes = {
    @Index(name = "idx_roleperm_role", columnList = "role_id"),
    @Index(name = "idx_roleperm_permission", columnList = "permission_id"),
    @Index(name = "uq_roleperm_role_perm", columnList = "role_id, permission_id", unique = true)
})
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public RolePermission() {}

    public RolePermission(Long roleId, Long permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
