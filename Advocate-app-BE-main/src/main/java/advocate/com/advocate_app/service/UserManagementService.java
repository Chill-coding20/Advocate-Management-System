package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Role;
import advocate.com.advocate_app.repository.AdvocateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserManagementService {

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private RbacService rbacService;

    public List<Advocate> getAllUsers() {
        return advocateRepository.findAll();
    }

    public Advocate getUser(Long id) {
        return advocateRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Advocate createUser(Advocate advocate) {
        if (advocateRepository.findByEmail(advocate.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        return advocateRepository.save(advocate);
    }

    public Advocate updateUser(Long id, Advocate updated) {
        Advocate user = getUser(id);
        user.setFullName(updated.getFullName());
        user.setEmail(updated.getEmail());
        user.setPhone(updated.getPhone());
        user.setBarCouncilId(updated.getBarCouncilId());
        user.setSpecialization(updated.getSpecialization());
        user.setExperience(updated.getExperience());
        user.setAddress(updated.getAddress());
        user.setRole(updated.getRole() != null ? updated.getRole() : user.getRole());
        return advocateRepository.save(user);
    }

    public void deleteUser(Long id) {
        rbacService.removeAllRolesFromAdvocate(id);
        advocateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Role> getUserRoles(Long userId) {
        return rbacService.getRolesForAdvocate(userId);
    }

    public void assignRole(Long userId, Long roleId) {
        rbacService.assignRoleToAdvocate(userId, roleId);
    }

    public void removeRole(Long userId, Long roleId) {
        rbacService.removeRoleFromAdvocate(userId, roleId);
    }

    public void setRoles(Long userId, List<Long> roleIds) {
        rbacService.removeAllRolesFromAdvocate(userId);
        for (Long roleId : roleIds) {
            rbacService.assignRoleToAdvocate(userId, roleId);
        }
    }
}
