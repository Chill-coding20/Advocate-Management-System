package advocate.com.advocate_app.websocket;

import advocate.com.advocate_app.security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String email = JwtUtil.extractEmail(token);
                    accessor.setUser(new UserPrincipal(email));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
        }
        return message;
    }
}
