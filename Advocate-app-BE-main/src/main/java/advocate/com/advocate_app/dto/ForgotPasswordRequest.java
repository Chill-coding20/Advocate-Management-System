package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
