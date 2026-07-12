package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "advocate")
public class Advocate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----- Identity -----
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Column(nullable = false, unique = true)
    private String barCouncilId;

    private String specialization;
    private int experience;
    private String address;

    // ----- Extended Profile -----
    private LocalDate dateOfBirth;
    private String gender;
    private LocalDate enrollmentDate;

    @Column(length = 2000)
    private String bio;

    // ----- Office -----
    private String officeName;
    private String officeAddress;
    private String city;
    private String state;
    private String country;
    private String pinCode;
    private String officePhone;
    private String officeEmail;
    private String website;
    private String gstNumber;
    private String panNumber;

    // ----- Branding -----
    private String profilePhotoPath;
    private String officeLogoPath;
    private String signaturePath;
    private String officeSealPath;
    private String primaryBrandColor;
    private String secondaryBrandColor;

    // ----- Preferences -----
    private String language;
    private String timeZone;
    private String currency;
    private String dateFormat;
    private Integer autoLogoutDuration;
    private String defaultDashboardFilter;

    // ----- Existing Fields -----
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'ADVOCATE'")
    private String role = "ADVOCATE";

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'light'")
    private String theme = "light";

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean whatsappEnabled = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean emailNotificationsEnabled = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean browserNotificationsEnabled = true;

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getOfficeName() { return officeName; }
    public void setOfficeName(String officeName) { this.officeName = officeName; }

    public String getOfficeAddress() { return officeAddress; }
    public void setOfficeAddress(String officeAddress) { this.officeAddress = officeAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }

    public String getOfficePhone() { return officePhone; }
    public void setOfficePhone(String officePhone) { this.officePhone = officePhone; }

    public String getOfficeEmail() { return officeEmail; }
    public void setOfficeEmail(String officeEmail) { this.officeEmail = officeEmail; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getProfilePhotoPath() { return profilePhotoPath; }
    public void setProfilePhotoPath(String profilePhotoPath) { this.profilePhotoPath = profilePhotoPath; }

    public String getOfficeLogoPath() { return officeLogoPath; }
    public void setOfficeLogoPath(String officeLogoPath) { this.officeLogoPath = officeLogoPath; }

    public String getSignaturePath() { return signaturePath; }
    public void setSignaturePath(String signaturePath) { this.signaturePath = signaturePath; }

    public String getOfficeSealPath() { return officeSealPath; }
    public void setOfficeSealPath(String officeSealPath) { this.officeSealPath = officeSealPath; }

    public String getPrimaryBrandColor() { return primaryBrandColor; }
    public void setPrimaryBrandColor(String primaryBrandColor) { this.primaryBrandColor = primaryBrandColor; }

    public String getSecondaryBrandColor() { return secondaryBrandColor; }
    public void setSecondaryBrandColor(String secondaryBrandColor) { this.secondaryBrandColor = secondaryBrandColor; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public Integer getAutoLogoutDuration() { return autoLogoutDuration; }
    public void setAutoLogoutDuration(Integer autoLogoutDuration) { this.autoLogoutDuration = autoLogoutDuration; }

    public String getDefaultDashboardFilter() { return defaultDashboardFilter; }
    public void setDefaultDashboardFilter(String defaultDashboardFilter) { this.defaultDashboardFilter = defaultDashboardFilter; }

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
}
