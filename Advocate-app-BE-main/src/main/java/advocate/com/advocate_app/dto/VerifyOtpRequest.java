package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP cannot be blank")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
    private String otp;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
