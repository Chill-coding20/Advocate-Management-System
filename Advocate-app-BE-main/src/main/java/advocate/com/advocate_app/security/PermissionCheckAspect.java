package advocate.com.advocate_app.security;

import advocate.com.advocate_app.service.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
public class PermissionCheckAspect {

    @Autowired
    private RbacService rbacService;

    @Around("@annotation(advocate.com.advocate_app.security.RequirePermission) || @within(advocate.com.advocate_app.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        RequirePermission methodAnnotation = signature.getMethod().getAnnotation(RequirePermission.class);
        RequirePermission classAnnotation = pjp.getTarget().getClass().getAnnotation(RequirePermission.class);

        RequirePermission annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        if (annotation == null) return pjp.proceed();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Long advocateId = (Long) request.getAttribute("authenticatedAdvocateId");

        if (advocateId == null) {
            throw new PermissionDeniedException("Authentication required");
        }

        Set<String> userPermissions = rbacService.getPermissionsForAdvocate(advocateId);
        String[] required = annotation.value();
        Logical logical = annotation.logical();

        boolean hasAccess;
        if (logical == Logical.AND) {
            hasAccess = Arrays.stream(required).allMatch(userPermissions::contains);
        } else {
            hasAccess = Arrays.stream(required).anyMatch(userPermissions::contains);
        }

        if (!hasAccess) {
            throw new PermissionDeniedException(
                    "Access denied. Required permissions: " + Arrays.toString(required)
            );
        }

        return pjp.proceed();
    }
}
