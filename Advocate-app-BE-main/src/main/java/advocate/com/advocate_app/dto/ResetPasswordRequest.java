package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ResetPasswordRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP cannot be blank")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
    private String otp;

    @NotBlank(message = "New password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*!?_+=-])[A-Za-z\\d@#$%^&*!?_+=-]{8,32}$",
            message = "Password must be 8-32 characters with at least one uppercase letter, one lowercase letter, one digit, and one special character (@ # $ % ^ & * ! ? _ + -)"
    )
    private String newPassword;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
