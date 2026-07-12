package advocate.com.advocate_app.websocket;

import advocate.com.advocate_app.websocket.event.EventType;
import advocate.com.advocate_app.websocket.dto.LiveEvent;
import advocate.com.advocate_app.websocket.dto.DashboardUpdateEvent;
import advocate.com.advocate_app.websocket.dto.ActivityEvent;
import advocate.com.advocate_app.websocket.dto.NotificationEvent;
import advocate.com.advocate_app.websocket.dto.SearchUpdateEvent;
import advocate.com.advocate_app.websocket.dto.HearingAlertEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String email, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(email, destination, payload);
    }

    public void sendNotification(String email, EventType eventType, String message, String entityType, Long entityId, String route) {
        NotificationEvent event = new NotificationEvent(
                eventType.name(), message, entityType, entityId, route, LocalDateTime.now().toString()
        );
        sendToUser(email, "/queue/notifications", event);
    }

    public void sendActivity(String email, EventType eventType, String message) {
        ActivityEvent event = new ActivityEvent(
                eventType.name(), message, LocalDateTime.now().toString()
        );
        sendToUser(email, "/queue/activity", event);
    }

    public void sendDashboardUpdate(String email) {
        DashboardUpdateEvent event = new DashboardUpdateEvent(
                System.currentTimeMillis()
        );
        sendToUser(email, "/queue/dashboard", event);
    }

    public void sendSearchUpdate(String email, String entityType, String action, Object entity) {
        SearchUpdateEvent event = new SearchUpdateEvent(
                entityType, action, entity, System.currentTimeMillis()
        );
        sendToUser(email, "/queue/search", event);
    }

    public void sendHearingAlert(String email, HearingAlertEvent event) {
        sendToUser(email, "/queue/hearing-alert", event);
    }
}
