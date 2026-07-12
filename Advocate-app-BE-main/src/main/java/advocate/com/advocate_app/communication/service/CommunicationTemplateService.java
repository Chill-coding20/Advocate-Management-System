package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.dto.NotificationTemplateDTO;
import advocate.com.advocate_app.communication.entity.NotificationTemplate;
import advocate.com.advocate_app.communication.repository.NotificationTemplateRepository;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunicationTemplateService {

    private final NotificationTemplateRepository templateRepository;

    public CommunicationTemplateService(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<NotificationTemplateDTO> getTemplates(Advocate advocate) {
        return templateRepository.findByAdvocateOrderByCreatedAtDesc(advocate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public NotificationTemplateDTO createTemplate(NotificationTemplateDTO dto, Advocate advocate) {
        NotificationTemplate template = new NotificationTemplate();
        template.setName(dto.getName());
        template.setChannel(dto.getChannel());
        template.setType(dto.getType());
        template.setSubjectTemplate(dto.getSubjectTemplate());
        template.setBodyTemplate(dto.getBodyTemplate());
        template.setActive(dto.isActive());
        template.setAdvocate(advocate);
        return toDTO(templateRepository.save(template));
    }

    public NotificationTemplateDTO updateTemplate(Long id, NotificationTemplateDTO dto, Advocate advocate) {
        NotificationTemplate template = templateRepository.findByIdAndAdvocate(id, advocate)
                .orElseThrow(() -> new RuntimeException("Template not found or unauthorized"));
        template.setName(dto.getName());
        template.setChannel(dto.getChannel());
        template.setType(dto.getType());
        template.setSubjectTemplate(dto.getSubjectTemplate());
        template.setBodyTemplate(dto.getBodyTemplate());
        template.setActive(dto.isActive());
        return toDTO(templateRepository.save(template));
    }

    public void deleteTemplate(Long id, Advocate advocate) {
        NotificationTemplate template = templateRepository.findByIdAndAdvocate(id, advocate)
                .orElseThrow(() -> new RuntimeException("Template not found or unauthorized"));
        templateRepository.delete(template);
    }

    private NotificationTemplateDTO toDTO(NotificationTemplate entity) {
        NotificationTemplateDTO dto = new NotificationTemplateDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setChannel(entity.getChannel());
        dto.setType(entity.getType());
        dto.setSubjectTemplate(entity.getSubjectTemplate());
        dto.setBodyTemplate(entity.getBodyTemplate());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
