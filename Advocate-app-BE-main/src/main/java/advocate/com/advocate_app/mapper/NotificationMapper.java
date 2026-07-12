package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.NotificationDTO;
import advocate.com.advocate_app.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(NotificationEntity notification) {
        if (notification == null) return null;
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadStatus(notification.isReadStatus());
        return dto;
    }
}
