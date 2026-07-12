package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ClientRequestDTO {
    @NotBlank(message = "Client name cannot be blank")
    private String name;

    @NotBlank(message = "Client email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Client phone cannot be blank")
    private String phone;

    private String address;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
