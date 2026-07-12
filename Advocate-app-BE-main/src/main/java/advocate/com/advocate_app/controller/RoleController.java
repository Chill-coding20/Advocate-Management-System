package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.entity.Role;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequirePermission("ROLE_MANAGE")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRole(id));
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return ResponseEntity.ok(roleService.createRole(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return ResponseEntity.ok(roleService.updateRole(id, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(Map.of("message", "Role deleted successfully"));
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<Long>> getRolePermissions(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getPermissionIdsForRole(id));
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<Map<String, String>> setRolePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.setPermissionsForRole(id, permissionIds);
        return ResponseEntity.ok(Map.of("message", "Permissions updated successfully"));
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Map<String, String>> assignPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        roleService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(Map.of("message", "Permission assigned successfully"));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Map<String, String>> removePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(Map.of("message", "Permission removed successfully"));
    }
}
