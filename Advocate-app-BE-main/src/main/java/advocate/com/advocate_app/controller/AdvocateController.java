package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.AdvocateProfileDTO;
import advocate.com.advocate_app.dto.LoginRequestDTO;
import advocate.com.advocate_app.dto.LoginResponseDTO;
import advocate.com.advocate_app.dto.SignupRequestDTO;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.AdvocateMapper;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.entity.Role;
import advocate.com.advocate_app.repository.RoleRepository;
import advocate.com.advocate_app.service.AdvocateService;
import advocate.com.advocate_app.service.AuditLogService;
import advocate.com.advocate_app.service.RbacService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/advocates")
public class AdvocateController {

    @Autowired
    private AdvocateService advocateService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private AdvocateMapper advocateMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private RbacService rbacService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequestDTO signupDTO) {
        Advocate advocate = advocateMapper.toEntity(signupDTO);
        if (advocate.getRole() == null || advocate.getRole().isBlank()) {
            advocate.setRole("ADVOCATE");
        }
        advocateService.registerUser(advocate);

        // Assign default RBAC role so the user can access protected endpoints
        roleRepository.findByName("Senior Advocate").ifPresent(role ->
            rbacService.assignRoleToAdvocate(advocate.getId(), role.getId())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginDTO, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String browser = parseBrowser(userAgent);
        String os = parseOS(userAgent);
        boolean success = advocateService.checkLogin(loginDTO.getEmail(), loginDTO.getPassword());
        if (success) {
            Advocate advocate = advocateRepository.findByEmail(loginDTO.getEmail()).orElse(null);
            String token = JwtUtil.generateToken(loginDTO.getEmail());
            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    "Login Successful!",
                    (advocate != null && advocate.getRole() != null) ? advocate.getRole() : "ADVOCATE",
                    (advocate != null && advocate.getTheme() != null) ? advocate.getTheme() : "light",
                    (advocate != null && advocate.getFullName() != null) ? advocate.getFullName() : "Advocate"
            );
            if (advocate != null) {
                auditLogService.recordAction(
                        advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                        AuditLogService.LOGIN, AuditLogService.MODULE_AUTH,
                        "Login Successful", "User logged in successfully",
                        "Authentication", null, "SUCCESS",
                        ip, "Desktop", browser, os, "POST", "/api/advocates/login", null
                );
            }
            return ResponseEntity.ok(response);
        } else {
            auditLogService.recordAction(
                    null, loginDTO.getEmail(),
                    AuditLogService.FAILED_LOGIN, AuditLogService.MODULE_AUTH,
                    "Login Failed", "Invalid credentials for " + loginDTO.getEmail(),
                    "Authentication", null, "FAILED",
                    ip, "Desktop", browser, os, "POST", "/api/advocates/login", null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password!"));
        }
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Edg")) return "Edge";
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        return "Unknown";
    }

    private String parseOS(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iOS")) return "iOS";
        return "Unknown";
    }

    @GetMapping("/profile")
    public ResponseEntity<AdvocateProfileDTO> getProfile(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        return ResponseEntity.ok(advocateMapper.toProfileDTO(advocate));
    }

    @PutMapping("/settings")
    public ResponseEntity<AdvocateProfileDTO> updateSettings(
            @RequestHeader("Authorization") String token,
            @RequestBody AdvocateProfileDTO settingsDTO) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));

        advocateMapper.updateEntityFromProfileDTO(settingsDTO, advocate);

        if (settingsDTO.getNewPassword() != null && !settingsDTO.getNewPassword().isBlank()) {
            advocate.setPassword(passwordEncoder.encode(settingsDTO.getNewPassword()));
        }

        Advocate saved = advocateRepository.save(advocate);

        auditLogService.recordAction(
                saved.getId(), saved.getFullName() != null ? saved.getFullName() : saved.getEmail(),
                AuditLogService.SETTINGS_UPDATED, AuditLogService.MODULE_SETTINGS,
                "Settings Updated", "Profile settings updated",
                "Settings", saved.getId(), "SUCCESS"
        );

        return ResponseEntity.ok(advocateMapper.toProfileDTO(saved));
    }

    /**
     * Lightweight endpoint to toggle notification preferences only.
     * Used by the Notification Center checkboxes in the frontend.
     * Does NOT touch required fields like fullName or barCouncilId.
     */
    @PatchMapping("/notification-settings")
    public ResponseEntity<Map<String, Boolean>> updateNotificationSettings(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Boolean> settings) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));

        if (settings.containsKey("whatsappEnabled")) {
            advocate.setWhatsappEnabled(settings.get("whatsappEnabled"));
        }
        if (settings.containsKey("emailNotificationsEnabled")) {
            advocate.setEmailNotificationsEnabled(settings.get("emailNotificationsEnabled"));
        }
        if (settings.containsKey("browserNotificationsEnabled")) {
            advocate.setBrowserNotificationsEnabled(settings.get("browserNotificationsEnabled"));
        }

        advocateRepository.save(advocate);

        auditLogService.recordAction(
                advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                AuditLogService.SETTINGS_UPDATED, AuditLogService.MODULE_SETTINGS,
                "Notification Settings Updated", "Notification preferences changed",
                "Settings", advocate.getId(), "SUCCESS"
        );

        return ResponseEntity.ok(Map.of(
                "whatsappEnabled", advocate.isWhatsappEnabled(),
                "emailNotificationsEnabled", advocate.isEmailNotificationsEnabled(),
                "browserNotificationsEnabled", advocate.isBrowserNotificationsEnabled()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email).orElse(null);
        if (advocate != null) {
            auditLogService.recordAction(
                    advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    AuditLogService.LOGOUT, AuditLogService.MODULE_AUTH,
                    "Logout", "User logged out",
                    "Authentication", null, "SUCCESS"
            );
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/my-permissions")
    public ResponseEntity<Set<String>> getMyPermissions(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        Set<String> permissions = rbacService.getPermissionsForAdvocate(advocate.getId());
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/my-roles")
    public ResponseEntity<List<String>> getMyRoles(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        List<String> roles = rbacService.getRoleNamesForAdvocate(advocate.getId());
        return ResponseEntity.ok(roles);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
