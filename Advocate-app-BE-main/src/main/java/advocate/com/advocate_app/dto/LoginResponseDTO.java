package advocate.com.advocate_app.dto;

public class LoginResponseDTO {
    private String token;
    private String message;
    private String role;
    private String theme;
    private String fullName;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, String message, String role, String theme, String fullName) {
        this.token = token;
        this.message = message;
        this.role = role;
        this.theme = theme;
        this.fullName = fullName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
