package advocate.com.advocate_app.dto;

public class AdvocateProfileDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String barCouncilId;
    private String specialization;
    private int experience;
    private String address;
    private String role;
    private String theme;
    private boolean whatsappEnabled;
    private boolean emailNotificationsEnabled;
    private boolean browserNotificationsEnabled;
    // Optional: only used when user wants to change password
    private String newPassword;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public boolean isWhatsappEnabled() { return whatsappEnabled; }
    public void setWhatsappEnabled(boolean whatsappEnabled) { this.whatsappEnabled = whatsappEnabled; }

    public boolean isEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }

    public boolean isBrowserNotificationsEnabled() { return browserNotificationsEnabled; }
    public void setBrowserNotificationsEnabled(boolean browserNotificationsEnabled) { this.browserNotificationsEnabled = browserNotificationsEnabled; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
