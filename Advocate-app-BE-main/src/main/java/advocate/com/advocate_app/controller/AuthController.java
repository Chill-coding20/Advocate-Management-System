package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.ForgotPasswordRequest;
import advocate.com.advocate_app.dto.ResetPasswordRequest;
import advocate.com.advocate_app.dto.VerifyOtpRequest;
import advocate.com.advocate_app.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestOtp(request.getEmail().trim().toLowerCase());
        return ResponseEntity.ok(Map.of("message", "If an account exists, an OTP has been sent."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean valid = passwordResetService.verifyOtp(
                request.getEmail().trim().toLowerCase(),
                request.getOtp().trim()
        );
        if (valid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "OTP verified."));
        }
        return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "Invalid or expired OTP."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean success = passwordResetService.resetPassword(
                request.getEmail().trim().toLowerCase(),
                request.getOtp().trim(),
                request.getNewPassword()
        );
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Password reset successful."));
        }
        return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "Invalid or expired OTP."));
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
