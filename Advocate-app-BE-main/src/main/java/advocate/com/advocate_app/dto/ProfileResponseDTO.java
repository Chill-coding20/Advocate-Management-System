package advocate.com.advocate_app.dto;

import java.time.LocalDate;

public class ProfileResponseDTO {

    // Identity
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String barCouncilId;
    private String specialization;
    private int experience;
    private String address;
    private String role;

    // Extended Profile
    private LocalDate dateOfBirth;
    private String gender;
    private LocalDate enrollmentDate;
    private String bio;
    private String practiceAreas;

    // Office
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

    // Branding
    private String profilePhotoUrl;
    private String officeLogoUrl;
    private String signatureUrl;
    private String officeSealUrl;
    private String primaryBrandColor;
    private String secondaryBrandColor;

    // Preferences
    private String theme;
    private String language;
    private String timeZone;
    private String currency;
    private String dateFormat;
    private Integer autoLogoutDuration;
    private String defaultDashboardFilter;

    // Notification flags (legacy)
    private boolean whatsappEnabled;
    private boolean emailNotificationsEnabled;
    private boolean browserNotificationsEnabled;

    // ===== Getters & Setters =====

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

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPracticeAreas() { return practiceAreas; }
    public void setPracticeAreas(String practiceAreas) { this.practiceAreas = practiceAreas; }

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

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getOfficeLogoUrl() { return officeLogoUrl; }
    public void setOfficeLogoUrl(String officeLogoUrl) { this.officeLogoUrl = officeLogoUrl; }

    public String getSignatureUrl() { return signatureUrl; }
    public void setSignatureUrl(String signatureUrl) { this.signatureUrl = signatureUrl; }

    public String getOfficeSealUrl() { return officeSealUrl; }
    public void setOfficeSealUrl(String officeSealUrl) { this.officeSealUrl = officeSealUrl; }

    public String getPrimaryBrandColor() { return primaryBrandColor; }
    public void setPrimaryBrandColor(String primaryBrandColor) { this.primaryBrandColor = primaryBrandColor; }

    public String getSecondaryBrandColor() { return secondaryBrandColor; }
    public void setSecondaryBrandColor(String secondaryBrandColor) { this.secondaryBrandColor = secondaryBrandColor; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

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

    public boolean isWhatsappEnabled() { return whatsappEnabled; }
    public void setWhatsappEnabled(boolean whatsappEnabled) { this.whatsappEnabled = whatsappEnabled; }

    public boolean isEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }

    public boolean isBrowserNotificationsEnabled() { return browserNotificationsEnabled; }
    public void setBrowserNotificationsEnabled(boolean browserNotificationsEnabled) { this.browserNotificationsEnabled = browserNotificationsEnabled; }
}
