package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Role;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequirePermission("USER_MANAGE")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Advocate> users = userManagementService.getAllUsers();
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("fullName", u.getFullName());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("barCouncilId", u.getBarCouncilId());
            m.put("specialization", u.getSpecialization());
            m.put("experience", u.getExperience());
            m.put("role", u.getRole());
            m.put("roles", userManagementService.getUserRoles(u.getId()).stream().map(Role::getName).collect(Collectors.toList()));
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        Advocate u = userManagementService.getUser(id);
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", u.getId());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("barCouncilId", u.getBarCouncilId());
        m.put("specialization", u.getSpecialization());
        m.put("experience", u.getExperience());
        m.put("role", u.getRole());
        m.put("roles", userManagementService.getUserRoles(u.getId()).stream().map(Role::getName).collect(Collectors.toList()));
        return ResponseEntity.ok(m);
    }

    @PostMapping
    public ResponseEntity<Advocate> createUser(@RequestBody Advocate advocate) {
        return ResponseEntity.ok(userManagementService.createUser(advocate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Advocate> updateUser(@PathVariable Long id, @RequestBody Advocate advocate) {
        return ResponseEntity.ok(userManagementService.updateUser(id, advocate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<List<Role>> getUserRoles(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUserRoles(id));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Map<String, String>> setUserRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userManagementService.setRoles(id, roleIds);
        return ResponseEntity.ok(Map.of("message", "Roles updated successfully"));
    }

    @PostMapping("/{id}/roles/{roleId}")
    public ResponseEntity<Map<String, String>> assignRole(@PathVariable Long id, @PathVariable Long roleId) {
        userManagementService.assignRole(id, roleId);
        return ResponseEntity.ok(Map.of("message", "Role assigned successfully"));
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<Map<String, String>> removeRole(@PathVariable Long id, @PathVariable Long roleId) {
        userManagementService.removeRole(id, roleId);
        return ResponseEntity.ok(Map.of("message", "Role removed successfully"));
    }
}
