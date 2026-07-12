package advocate.com.advocate_app.security;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.service.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private RbacService rbacService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.contains("/api/advocates/login") || path.contains("/api/advocates/signup")
                || path.contains("/api/auth/") || path.contains("/api/whatsapp/webhook")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized: Missing or invalid token format\"}");
            return false;
        }

        try {
            String token = authHeader.substring(7);
            String email = JwtUtil.extractEmail(token);
            request.setAttribute("authenticatedEmail", email);

            Advocate advocate = advocateRepository.findByEmail(email).orElse(null);
            if (advocate != null) {
                request.setAttribute("authenticatedAdvocateId", advocate.getId());
                Set<String> permissions = rbacService.getPermissionsForAdvocate(advocate.getId());
                request.setAttribute("authenticatedPermissions", permissions);
            }

            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized: Invalid or expired token\"}");
            return false;
        }
    }
}
