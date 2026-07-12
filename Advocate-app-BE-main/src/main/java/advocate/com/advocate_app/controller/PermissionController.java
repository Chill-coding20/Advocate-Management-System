package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.entity.Permission;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermission(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getPermission(id));
    }

    @PostMapping
    @RequirePermission("ROLE_MANAGE")
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        return ResponseEntity.ok(permissionService.createPermission(permission));
    }

    @PutMapping("/{id}")
    @RequirePermission("ROLE_MANAGE")
    public ResponseEntity<Permission> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        return ResponseEntity.ok(permissionService.updatePermission(id, permission));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("ROLE_MANAGE")
    public ResponseEntity<Map<String, String>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(Map.of("message", "Permission deleted successfully"));
    }
}
