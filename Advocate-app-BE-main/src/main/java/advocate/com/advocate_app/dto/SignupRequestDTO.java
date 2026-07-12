package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SignupRequestDTO {
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*!?_+=-])[A-Za-z\\d@#$%^&*!?_+=-]{8,32}$",
            message = "Password must be 8-32 characters with at least one uppercase letter, one lowercase letter, one digit, and one special character (@ # $ % ^ & * ! ? _ + -)"
    )
    private String password;

    private String phone;

    @NotBlank(message = "Bar Council ID cannot be blank")
    private String barCouncilId;

    private String specialization;
    private int experience;
    private String address;
    private String role;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBarCouncilId() { return barCouncilId; }
    public void setBarCouncilId(String barCouncilId) { this.barCouncilId = barCouncilId; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
