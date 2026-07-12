package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Permission;
import advocate.com.advocate_app.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission getPermission(Long id) {
        return permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Permission not found"));
    }

    public Permission createPermission(Permission permission) {
        if (permissionRepository.existsByName(permission.getName())) {
            throw new RuntimeException("Permission already exists");
        }
        return permissionRepository.save(permission);
    }

    public Permission updatePermission(Long id, Permission updated) {
        Permission p = getPermission(id);
        p.setName(updated.getName());
        p.setDescription(updated.getDescription());
        p.setModule(updated.getModule());
        return permissionRepository.save(p);
    }

    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }
}
