package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.CaseEventRequestDTO;
import advocate.com.advocate_app.dto.CaseEventResponseDTO;
import advocate.com.advocate_app.entity.CaseEventEntity;
import org.springframework.stereotype.Component;

@Component
public class CaseEventMapper {

    public CaseEventResponseDTO toResponseDTO(CaseEventEntity event) {
        if (event == null) return null;
        CaseEventResponseDTO dto = new CaseEventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setEventType(event.getEventType());
        dto.setDescription(event.getDescription());
        dto.setDate(event.getDate());
        dto.setTime(event.getTime());
        dto.setNotified(event.isNotified());
        if (event.getCaseEntity() != null) {
            dto.setCaseEntity(new CaseEventResponseDTO.CaseEntityInfo(
                    event.getCaseEntity().getId(),
                    event.getCaseEntity().getCaseNumber(),
                    event.getCaseEntity().getCaseTitle()
            ));
        }
        return dto;
    }

    public CaseEventEntity toEntity(CaseEventRequestDTO dto) {
        if (dto == null) return null;
        CaseEventEntity event = new CaseEventEntity();
        event.setTitle(dto.getTitle());
        event.setEventType(dto.getEventType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setTime(dto.getTime());
        event.setNotified(false);
        return event;
    }

    public void updateEntityFromRequestDTO(CaseEventRequestDTO dto, CaseEventEntity event) {
        if (dto == null || event == null) return;
        event.setTitle(dto.getTitle());
        event.setEventType(dto.getEventType());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setTime(dto.getTime());
    }
}
