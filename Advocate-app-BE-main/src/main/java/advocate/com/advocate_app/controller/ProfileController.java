package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.*;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.AuditLogService;
import advocate.com.advocate_app.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @GetMapping
    public ResponseEntity<ProfileResponseDTO> getProfile(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(profileService.getProfile(email));
    }

    @PutMapping
    @RequirePermission("PROFILE_EDIT")
    public ResponseEntity<ProfileResponseDTO> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody ProfileUpdateRequestDTO dto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        ProfileResponseDTO result = profileService.updateProfile(email, dto);
        Advocate advocate = advocateRepository.findByEmail(email).orElse(null);
        if (advocate != null) {
            auditLogService.recordAction(
                    advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    AuditLogService.PROFILE_UPDATED, AuditLogService.MODULE_PROFILE,
                    "Profile Updated", "Profile details updated",
                    "Profile", advocate.getId(), "SUCCESS"
            );
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/branding/{type}")
    public ResponseEntity<ProfileResponseDTO> uploadBrandingImage(
            @RequestHeader("Authorization") String token,
            @PathVariable String type,
            @RequestParam("file") MultipartFile file) {
        String email = JwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(profileService.uploadBrandingImage(email, file, type));
    }

    @PutMapping("/preferences")
    @RequirePermission("PROFILE_EDIT")
    public ResponseEntity<ProfileResponseDTO> updatePreferences(
            @RequestHeader("Authorization") String token,
            @RequestBody PreferencesRequestDTO dto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(profileService.updatePreferences(email, dto));
    }

    @PutMapping("/change-password")
    @RequirePermission("PROFILE_EDIT")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordRequestDTO dto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        try {
            profileService.changePassword(email, dto);
            Advocate advocate = advocateRepository.findByEmail(email).orElse(null);
            if (advocate != null) {
                auditLogService.recordAction(
                        advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                        AuditLogService.PASSWORD_CHANGED, AuditLogService.MODULE_PROFILE,
                        "Password Changed", "Account password changed",
                        "Profile", advocate.getId(), "SUCCESS"
                );
            }
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/files/{subDir}/{filename}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String subDir,
            @PathVariable String filename) {
        try {
            Resource resource = profileService.loadBrandingFile(subDir + "/" + filename);
            String contentType = determineContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
