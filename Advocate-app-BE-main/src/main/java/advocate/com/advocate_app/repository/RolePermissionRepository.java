package advocate.com.advocate_app.repository;

import advocate.com.advocate_app.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
    List<RolePermission> findByPermissionId(Long permissionId);
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);
    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);
    void deleteByRoleId(Long roleId);
}
