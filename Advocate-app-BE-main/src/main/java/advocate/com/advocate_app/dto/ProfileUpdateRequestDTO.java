package advocate.com.advocate_app.dto;

import java.time.LocalDate;

public class ProfileUpdateRequestDTO {

    // General
    private String fullName;
    private String phone;
    private String specialization;
    private Integer experience;
    private String address;
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

    // Branding Colors
    private String primaryBrandColor;
    private String secondaryBrandColor;

    // Notification flags
    private Boolean whatsappEnabled;
    private Boolean emailNotificationsEnabled;
    private Boolean browserNotificationsEnabled;

    // ===== Getters & Setters =====

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

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

    public String getPrimaryBrandColor() { return primaryBrandColor; }
    public void setPrimaryBrandColor(String primaryBrandColor) { this.primaryBrandColor = primaryBrandColor; }

    public String getSecondaryBrandColor() { return secondaryBrandColor; }
    public void setSecondaryBrandColor(String secondaryBrandColor) { this.secondaryBrandColor = secondaryBrandColor; }

    public Boolean getWhatsappEnabled() { return whatsappEnabled; }
    public void setWhatsappEnabled(Boolean whatsappEnabled) { this.whatsappEnabled = whatsappEnabled; }

    public Boolean getEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }

    public Boolean getBrowserNotificationsEnabled() { return browserNotificationsEnabled; }
    public void setBrowserNotificationsEnabled(Boolean browserNotificationsEnabled) { this.browserNotificationsEnabled = browserNotificationsEnabled; }
}
