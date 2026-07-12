package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.ActivityResponseDTO;
import advocate.com.advocate_app.entity.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public ActivityResponseDTO toResponseDTO(Activity activity) {
        if (activity == null) return null;
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(activity.getId());
        dto.setDescription(activity.getDescription());
        dto.setActionType(activity.getActionType());
        dto.setTimestamp(activity.getTimestamp());
        return dto;
    }
}
