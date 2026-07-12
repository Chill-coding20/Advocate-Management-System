package advocate.com.advocate_app.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String[] value();
    Logical logical() default Logical.OR;
}
