package advocate.com.advocate_app.config;

import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Order(1)
public class RbacDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RbacDataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private AdvocateRoleRepository advocateRoleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            seedRolesAndPermissions();
        }

        backfillMissingRoles();
    }

    private void seedRolesAndPermissions() {
        // Create Permissions
        Map<String, Permission> perms = new LinkedHashMap<>();
        String[][] permDefs = {
            {"CLIENT_VIEW", "View clients", "CLIENTS"},
            {"CLIENT_CREATE", "Create new clients", "CLIENTS"},
            {"CLIENT_EDIT", "Edit existing clients", "CLIENTS"},
            {"CLIENT_DELETE", "Delete clients", "CLIENTS"},
            {"CASE_VIEW", "View cases", "CASES"},
            {"CASE_CREATE", "Create new cases", "CASES"},
            {"CASE_EDIT", "Edit existing cases", "CASES"},
            {"CASE_DELETE", "Delete cases", "CASES"},
            {"PAYMENT_VIEW", "View payments", "PAYMENTS"},
            {"PAYMENT_CREATE", "Create payments", "PAYMENTS"},
            {"PAYMENT_EDIT", "Edit payments", "PAYMENTS"},
            {"EXPENSE_VIEW", "View expenses", "EXPENSES"},
            {"EXPENSE_CREATE", "Create expenses", "EXPENSES"},
            {"EXPENSE_EDIT", "Edit expenses", "EXPENSES"},
            {"EXPENSE_DELETE", "Delete expenses", "EXPENSES"},
            {"INVOICE_VIEW", "View invoices", "INVOICES"},
            {"INVOICE_CREATE", "Create invoices", "INVOICES"},
            {"INVOICE_EDIT", "Edit invoices", "INVOICES"},
            {"DOCUMENT_VIEW", "View documents", "DOCUMENTS"},
            {"DOCUMENT_UPLOAD", "Upload documents", "DOCUMENTS"},
            {"DOCUMENT_EDIT", "Edit document metadata", "DOCUMENTS"},
            {"DOCUMENT_DELETE", "Delete documents", "DOCUMENTS"},
            {"TASK_VIEW", "View tasks", "TASKS"},
            {"TASK_CREATE", "Create tasks", "TASKS"},
            {"TASK_EDIT", "Edit tasks", "TASKS"},
            {"TASK_DELETE", "Delete tasks", "TASKS"},
            {"EVENT_VIEW", "View hearings/events", "EVENTS"},
            {"EVENT_CREATE", "Create hearings/events", "EVENTS"},
            {"EVENT_DELETE", "Delete hearings/events", "EVENTS"},
            {"REPORT_VIEW", "View reports", "REPORTS"},
            {"REPORT_EXPORT", "Export reports (PDF/CSV)", "REPORTS"},
            {"PROFILE_EDIT", "Edit own profile", "SETTINGS"},
            {"SETTINGS_EDIT", "Edit system settings", "SETTINGS"},
            {"USER_MANAGE", "Manage users", "ADMIN"},
            {"ROLE_MANAGE", "Manage roles and permissions", "ADMIN"},
        };
        for (String[] def : permDefs) {
            Permission p = new Permission(def[0], def[1], def[2]);
            perms.put(def[0], permissionRepository.save(p));
        }

        // Create Roles
        Map<String, Role> roles = new LinkedHashMap<>();
        String[][] roleDefs = {
            {"Super Admin", "Full system access with all permissions"},
            {"Senior Advocate", "Senior advocate with full case/client management and financial access"},
            {"Junior Advocate", "Junior advocate with limited case management (no delete)"},
            {"Accountant", "Financial module access only"},
            {"Receptionist", "Front desk with client and scheduling access"},
            {"Intern", "Read-only access across the system"},
        };
        for (String[] def : roleDefs) {
            roles.put(def[0], roleRepository.save(new Role(def[0], def[1])));
        }

        // Assign Permissions to Roles
        Map<String, List<String>> rolePermMap = new LinkedHashMap<>();

        rolePermMap.put("Super Admin", Arrays.asList(
            "CLIENT_VIEW", "CLIENT_CREATE", "CLIENT_EDIT", "CLIENT_DELETE",
            "CASE_VIEW", "CASE_CREATE", "CASE_EDIT", "CASE_DELETE",
            "PAYMENT_VIEW", "PAYMENT_CREATE", "PAYMENT_EDIT",
            "EXPENSE_VIEW", "EXPENSE_CREATE", "EXPENSE_EDIT", "EXPENSE_DELETE",
            "INVOICE_VIEW", "INVOICE_CREATE", "INVOICE_EDIT",
            "DOCUMENT_VIEW", "DOCUMENT_UPLOAD", "DOCUMENT_EDIT", "DOCUMENT_DELETE",
            "TASK_VIEW", "TASK_CREATE", "TASK_EDIT", "TASK_DELETE",
            "EVENT_VIEW", "EVENT_CREATE", "EVENT_DELETE",
            "REPORT_VIEW", "REPORT_EXPORT",
            "PROFILE_EDIT", "SETTINGS_EDIT",
            "USER_MANAGE", "ROLE_MANAGE"
        ));

        rolePermMap.put("Senior Advocate", Arrays.asList(
            "CLIENT_VIEW", "CLIENT_CREATE", "CLIENT_EDIT",
            "CASE_VIEW", "CASE_CREATE", "CASE_EDIT", "CASE_DELETE",
            "PAYMENT_VIEW", "PAYMENT_CREATE", "PAYMENT_EDIT",
            "EXPENSE_VIEW", "EXPENSE_CREATE", "EXPENSE_EDIT", "EXPENSE_DELETE",
            "INVOICE_VIEW", "INVOICE_CREATE", "INVOICE_EDIT",
            "DOCUMENT_VIEW", "DOCUMENT_UPLOAD", "DOCUMENT_EDIT", "DOCUMENT_DELETE",
            "TASK_VIEW", "TASK_CREATE", "TASK_EDIT", "TASK_DELETE",
            "EVENT_VIEW", "EVENT_CREATE", "EVENT_DELETE",
            "REPORT_VIEW", "REPORT_EXPORT",
            "PROFILE_EDIT"
        ));

        rolePermMap.put("Junior Advocate", Arrays.asList(
            "CLIENT_VIEW",
            "CASE_VIEW", "CASE_CREATE", "CASE_EDIT",
            "PAYMENT_VIEW",
            "EXPENSE_VIEW",
            "INVOICE_VIEW",
            "DOCUMENT_VIEW", "DOCUMENT_UPLOAD",
            "TASK_VIEW", "TASK_CREATE", "TASK_EDIT",
            "EVENT_VIEW", "EVENT_CREATE",
            "REPORT_VIEW",
            "PROFILE_EDIT"
        ));

        rolePermMap.put("Accountant", Arrays.asList(
            "PAYMENT_VIEW", "PAYMENT_CREATE", "PAYMENT_EDIT",
            "EXPENSE_VIEW", "EXPENSE_CREATE", "EXPENSE_EDIT", "EXPENSE_DELETE",
            "INVOICE_VIEW", "INVOICE_CREATE", "INVOICE_EDIT",
            "REPORT_VIEW", "REPORT_EXPORT",
            "TASK_VIEW",
            "PROFILE_EDIT"
        ));

        rolePermMap.put("Receptionist", Arrays.asList(
            "CLIENT_VIEW", "CLIENT_CREATE", "CLIENT_EDIT",
            "CASE_VIEW",
            "EVENT_VIEW", "EVENT_CREATE",
            "TASK_VIEW",
            "PROFILE_EDIT"
        ));

        rolePermMap.put("Intern", Arrays.asList(
            "CLIENT_VIEW",
            "CASE_VIEW",
            "DOCUMENT_VIEW",
            "EVENT_VIEW",
            "TASK_VIEW",
            "PROFILE_EDIT"
        ));

        for (Map.Entry<String, List<String>> entry : rolePermMap.entrySet()) {
            Role role = roles.get(entry.getKey());
            for (String permName : entry.getValue()) {
                Permission perm = perms.get(permName);
                if (perm != null) {
                    rolePermissionRepository.save(new RolePermission(role.getId(), perm.getId()));
                }
            }
        }
    }

    private void backfillMissingRoles() {
        Role defaultRole = roleRepository.findByName("Senior Advocate").orElse(null);
        if (defaultRole == null) {
            log.warn("Default role 'Senior Advocate' not found. Skipping RBAC backfill.");
            return;
        }

        List<Advocate> allAdvocates = advocateRepository.findAll();
        int assigned = 0;
        for (Advocate advocate : allAdvocates) {
            List<AdvocateRole> existingRoles = advocateRoleRepository.findByAdvocateId(advocate.getId());
            if (existingRoles.isEmpty()) {
                advocateRoleRepository.save(new AdvocateRole(advocate.getId(), defaultRole.getId()));
                assigned++;
            }
        }
        if (assigned > 0) {
            log.info("Assigned default RBAC role 'Senior Advocate' to {} existing advocate(s).", assigned);
        }
    }
}
