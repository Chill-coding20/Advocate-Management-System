package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RbacService {

    @Autowired
    private AdvocateRoleRepository advocateRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Set<String> getPermissionsForAdvocate(Long advocateId) {
        List<AdvocateRole> advocateRoles = advocateRoleRepository.findByAdvocateId(advocateId);
        if (advocateRoles.isEmpty()) return Collections.emptySet();

        Set<String> permissions = new HashSet<>();
        for (AdvocateRole ar : advocateRoles) {
            List<RolePermission> rps = rolePermissionRepository.findByRoleId(ar.getRoleId());
            for (RolePermission rp : rps) {
                Permission p = permissionRepository.findById(rp.getPermissionId()).orElse(null);
                if (p != null) permissions.add(p.getName());
            }
        }
        return permissions;
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Long advocateId, String permissionName) {
        return getPermissionsForAdvocate(advocateId).contains(permissionName);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyPermission(Long advocateId, String... permissionNames) {
        Set<String> userPerms = getPermissionsForAdvocate(advocateId);
        for (String p : permissionNames) {
            if (userPerms.contains(p)) return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean hasAllPermissions(Long advocateId, String... permissionNames) {
        Set<String> userPerms = getPermissionsForAdvocate(advocateId);
        for (String p : permissionNames) {
            if (!userPerms.contains(p)) return false;
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<Role> getRolesForAdvocate(Long advocateId) {
        return advocateRoleRepository.findByAdvocateId(advocateId).stream()
                .map(ar -> roleRepository.findById(ar.getRoleId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getRoleNamesForAdvocate(Long advocateId) {
        return getRolesForAdvocate(advocateId).stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    public void assignRoleToAdvocate(Long advocateId, Long roleId) {
        if (!advocateRoleRepository.existsByAdvocateIdAndRoleId(advocateId, roleId)) {
            advocateRoleRepository.save(new AdvocateRole(advocateId, roleId));
        }
    }

    public void removeRoleFromAdvocate(Long advocateId, Long roleId) {
        advocateRoleRepository.deleteByAdvocateIdAndRoleId(advocateId, roleId);
    }

    public void removeAllRolesFromAdvocate(Long advocateId) {
        advocateRoleRepository.deleteByAdvocateId(advocateId);
    }
}
