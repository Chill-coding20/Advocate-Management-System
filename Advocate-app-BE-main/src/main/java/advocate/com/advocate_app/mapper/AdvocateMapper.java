package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.AdvocateProfileDTO;
import advocate.com.advocate_app.dto.SignupRequestDTO;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.stereotype.Component;

@Component
public class AdvocateMapper {

    public AdvocateProfileDTO toProfileDTO(Advocate advocate) {
        if (advocate == null) return null;
        AdvocateProfileDTO dto = new AdvocateProfileDTO();
        dto.setId(advocate.getId());
        dto.setFullName(advocate.getFullName());
        dto.setEmail(advocate.getEmail());
        dto.setPhone(advocate.getPhone());
        dto.setBarCouncilId(advocate.getBarCouncilId());
        dto.setSpecialization(advocate.getSpecialization());
        dto.setExperience(advocate.getExperience());
        dto.setAddress(advocate.getAddress());
        dto.setRole(advocate.getRole());
        dto.setTheme(advocate.getTheme());
        dto.setWhatsappEnabled(advocate.isWhatsappEnabled());
        dto.setEmailNotificationsEnabled(advocate.isEmailNotificationsEnabled());
        dto.setBrowserNotificationsEnabled(advocate.isBrowserNotificationsEnabled());
        return dto;
    }

    public Advocate toEntity(SignupRequestDTO dto) {
        if (dto == null) return null;
        Advocate advocate = new Advocate();
        advocate.setFullName(dto.getFullName());
        advocate.setEmail(dto.getEmail());
        advocate.setPassword(dto.getPassword());
        advocate.setPhone(dto.getPhone());
        advocate.setBarCouncilId(dto.getBarCouncilId());
        advocate.setSpecialization(dto.getSpecialization());
        advocate.setExperience(dto.getExperience());
        advocate.setAddress(dto.getAddress());
        if (dto.getRole() != null) {
            advocate.setRole(dto.getRole());
        }
        return advocate;
    }
    
    public void updateEntityFromProfileDTO(AdvocateProfileDTO dto, Advocate advocate) {
        if (dto == null || advocate == null) return;
        advocate.setFullName(dto.getFullName());
        advocate.setPhone(dto.getPhone());
        advocate.setBarCouncilId(dto.getBarCouncilId());
        advocate.setSpecialization(dto.getSpecialization());
        advocate.setExperience(dto.getExperience());
        advocate.setAddress(dto.getAddress());
        if (dto.getTheme() != null) {
            advocate.setTheme(dto.getTheme());
        }
        advocate.setWhatsappEnabled(dto.isWhatsappEnabled());
        advocate.setEmailNotificationsEnabled(dto.isEmailNotificationsEnabled());
        advocate.setBrowserNotificationsEnabled(dto.isBrowserNotificationsEnabled());
    }
}
