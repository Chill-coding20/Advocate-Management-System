package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.*;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.repository.AdvocateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${app.document.upload-dir:uploads}")
    private String uploadDir;

    public ProfileResponseDTO getProfile(String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        return toProfileResponse(advocate);
    }

    public ProfileResponseDTO updateProfile(String email, ProfileUpdateRequestDTO dto) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));

        if (dto.getFullName() != null) advocate.setFullName(dto.getFullName());
        if (dto.getPhone() != null) advocate.setPhone(dto.getPhone());
        if (dto.getSpecialization() != null) advocate.setSpecialization(dto.getSpecialization());
        if (dto.getExperience() != null) advocate.setExperience(dto.getExperience());
        if (dto.getAddress() != null) advocate.setAddress(dto.getAddress());
        if (dto.getDateOfBirth() != null) advocate.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) advocate.setGender(dto.getGender());
        if (dto.getEnrollmentDate() != null) advocate.setEnrollmentDate(dto.getEnrollmentDate());
        if (dto.getBio() != null) advocate.setBio(dto.getBio());
        if (dto.getPracticeAreas() != null) advocate.setSpecialization(dto.getPracticeAreas());
        if (dto.getOfficeName() != null) advocate.setOfficeName(dto.getOfficeName());
        if (dto.getOfficeAddress() != null) advocate.setOfficeAddress(dto.getOfficeAddress());
        if (dto.getCity() != null) advocate.setCity(dto.getCity());
        if (dto.getState() != null) advocate.setState(dto.getState());
        if (dto.getCountry() != null) advocate.setCountry(dto.getCountry());
        if (dto.getPinCode() != null) advocate.setPinCode(dto.getPinCode());
        if (dto.getOfficePhone() != null) advocate.setOfficePhone(dto.getOfficePhone());
        if (dto.getOfficeEmail() != null) advocate.setOfficeEmail(dto.getOfficeEmail());
        if (dto.getWebsite() != null) advocate.setWebsite(dto.getWebsite());
        if (dto.getGstNumber() != null) advocate.setGstNumber(dto.getGstNumber());
        if (dto.getPanNumber() != null) advocate.setPanNumber(dto.getPanNumber());
        if (dto.getPrimaryBrandColor() != null) advocate.setPrimaryBrandColor(dto.getPrimaryBrandColor());
        if (dto.getSecondaryBrandColor() != null) advocate.setSecondaryBrandColor(dto.getSecondaryBrandColor());
        if (dto.getWhatsappEnabled() != null) advocate.setWhatsappEnabled(dto.getWhatsappEnabled());
        if (dto.getEmailNotificationsEnabled() != null) advocate.setEmailNotificationsEnabled(dto.getEmailNotificationsEnabled());
        if (dto.getBrowserNotificationsEnabled() != null) advocate.setBrowserNotificationsEnabled(dto.getBrowserNotificationsEnabled());

        Advocate saved = advocateRepository.save(advocate);
        return toProfileResponse(saved);
    }

    public ProfileResponseDTO uploadBrandingImage(String email, MultipartFile file, String type) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String storedPath = storeFile(file, "branding");

        switch (type) {
            case "photo" -> advocate.setProfilePhotoPath(storedPath);
            case "logo" -> advocate.setOfficeLogoPath(storedPath);
            case "signature" -> advocate.setSignaturePath(storedPath);
            case "seal" -> advocate.setOfficeSealPath(storedPath);
            default -> throw new IllegalArgumentException("Unknown branding type: " + type);
        }

        Advocate saved = advocateRepository.save(advocate);
        return toProfileResponse(saved);
    }

    public ProfileResponseDTO updatePreferences(String email, PreferencesRequestDTO dto) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));

        if (dto.getTheme() != null) advocate.setTheme(dto.getTheme());
        if (dto.getLanguage() != null) advocate.setLanguage(dto.getLanguage());
        if (dto.getTimeZone() != null) advocate.setTimeZone(dto.getTimeZone());
        if (dto.getCurrency() != null) advocate.setCurrency(dto.getCurrency());
        if (dto.getDateFormat() != null) advocate.setDateFormat(dto.getDateFormat());
        if (dto.getAutoLogoutDuration() != null) advocate.setAutoLogoutDuration(dto.getAutoLogoutDuration());
        if (dto.getDefaultDashboardFilter() != null) advocate.setDefaultDashboardFilter(dto.getDefaultDashboardFilter());

        Advocate saved = advocateRepository.save(advocate);
        return toProfileResponse(saved);
    }

    public void changePassword(String email, ChangePasswordRequestDTO dto) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));

        String stored = advocate.getPassword();
        boolean currentPasswordValid;
        if (AdvocateService.isBcryptHash(stored)) {
            currentPasswordValid = passwordEncoder.matches(dto.getCurrentPassword(), stored);
        } else {
            currentPasswordValid = stored.equals(dto.getCurrentPassword());
        }
        if (!currentPasswordValid) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password cannot be blank");
        }

        String pwd = dto.getNewPassword();
        if (!pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*!?_+=-])[A-Za-z\\d@#$%^&*!?_+=-]{8,32}$")) {
            throw new IllegalArgumentException("Password must be 8-32 characters with at least one uppercase letter, one lowercase letter, one digit, and one special character (@ # $ % ^ & * ! ? _ + -)");
        }

        advocate.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        advocateRepository.save(advocate);
    }

    public Resource loadBrandingFile(String filePath) throws IOException {
        try {
            Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path path = rootPath.resolve(filePath).normalize();
            if (!path.startsWith(rootPath)) {
                throw new IOException("Access denied");
            }
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("File not found or not readable");
            }
        } catch (MalformedURLException e) {
            throw new IOException("File not found", e);
        }
    }

    private String storeFile(MultipartFile file, String subDir) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                originalName = "unnamed_file";
            }

            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }
            String storedName = UUID.randomUUID().toString() + extension;

            Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetDir = rootPath.resolve(subDir);
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(storedName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored branding file: {} -> {}", originalName, targetPath);

            // Return relative path for DB storage
            return subDir + "/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private ProfileResponseDTO toProfileResponse(Advocate a) {
        ProfileResponseDTO dto = new ProfileResponseDTO();

        dto.setId(a.getId());
        dto.setFullName(a.getFullName());
        dto.setEmail(a.getEmail());
        dto.setPhone(a.getPhone());
        dto.setBarCouncilId(a.getBarCouncilId());
        dto.setSpecialization(a.getSpecialization());
        dto.setExperience(a.getExperience());
        dto.setAddress(a.getAddress());
        dto.setRole(a.getRole());

        dto.setDateOfBirth(a.getDateOfBirth());
        dto.setGender(a.getGender());
        dto.setEnrollmentDate(a.getEnrollmentDate());
        dto.setBio(a.getBio());
        dto.setPracticeAreas(a.getSpecialization());

        dto.setOfficeName(a.getOfficeName());
        dto.setOfficeAddress(a.getOfficeAddress());
        dto.setCity(a.getCity());
        dto.setState(a.getState());
        dto.setCountry(a.getCountry());
        dto.setPinCode(a.getPinCode());
        dto.setOfficePhone(a.getOfficePhone());
        dto.setOfficeEmail(a.getOfficeEmail());
        dto.setWebsite(a.getWebsite());
        dto.setGstNumber(a.getGstNumber());
        dto.setPanNumber(a.getPanNumber());

        String baseUrl = "/api/profile/files/";
        if (a.getProfilePhotoPath() != null) dto.setProfilePhotoUrl(baseUrl + a.getProfilePhotoPath());
        if (a.getOfficeLogoPath() != null) dto.setOfficeLogoUrl(baseUrl + a.getOfficeLogoPath());
        if (a.getSignaturePath() != null) dto.setSignatureUrl(baseUrl + a.getSignaturePath());
        if (a.getOfficeSealPath() != null) dto.setOfficeSealUrl(baseUrl + a.getOfficeSealPath());
        dto.setPrimaryBrandColor(a.getPrimaryBrandColor());
        dto.setSecondaryBrandColor(a.getSecondaryBrandColor());

        dto.setTheme(a.getTheme());
        dto.setLanguage(a.getLanguage());
        dto.setTimeZone(a.getTimeZone());
        dto.setCurrency(a.getCurrency());
        dto.setDateFormat(a.getDateFormat());
        dto.setAutoLogoutDuration(a.getAutoLogoutDuration());
        dto.setDefaultDashboardFilter(a.getDefaultDashboardFilter());

        dto.setWhatsappEnabled(a.isWhatsappEnabled());
        dto.setEmailNotificationsEnabled(a.isEmailNotificationsEnabled());
        dto.setBrowserNotificationsEnabled(a.isBrowserNotificationsEnabled());

        return dto;
    }
}
