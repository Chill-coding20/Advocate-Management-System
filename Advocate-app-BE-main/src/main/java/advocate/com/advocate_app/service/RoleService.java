package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Permission;
import advocate.com.advocate_app.entity.Role;
import advocate.com.advocate_app.entity.RolePermission;
import advocate.com.advocate_app.repository.PermissionRepository;
import advocate.com.advocate_app.repository.RolePermissionRepository;
import advocate.com.advocate_app.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRole(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("Role already exists");
        }
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role updated) {
        Role role = getRole(id);
        role.setName(updated.getName());
        role.setDescription(updated.getDescription());
        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        rolePermissionRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Permission> getPermissionsForRole(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(rp -> permissionRepository.findById(rp.getPermissionId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getPermissionIdsForRole(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    public void assignPermissionToRole(Long roleId, Long permissionId) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            rolePermissionRepository.save(new RolePermission(roleId, permissionId));
        }
    }

    public void removePermissionFromRole(Long roleId, Long permissionId) {
        rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    public void setPermissionsForRole(Long roleId, List<Long> permissionIds) {
        rolePermissionRepository.deleteByRoleId(roleId);
        for (Long permId : permissionIds) {
            rolePermissionRepository.save(new RolePermission(roleId, permId));
        }
    }
}
