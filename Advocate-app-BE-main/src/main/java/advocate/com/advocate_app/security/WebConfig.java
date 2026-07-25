package advocate.com.advocate_app.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${app.cors.allowed-origins:${CORS_ORIGINS:http://localhost:5173}}")
    private String allowedOrigins;

    @PostConstruct
    public void logCorsConfig() {
        log.info("=== CORS Configuration ===");
        log.info("Resolved allowed origins from config/env: '{}'", allowedOrigins);
        if (allowedOrigins != null) {
            String[] parts = allowedOrigins.split(",");
            log.info("  Config provides {} origin pattern(s)", parts.length);
            for (int i = 0; i < parts.length; i++) {
                log.info("    [{}] '{}'", i, parts[i].trim());
            }
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering JwtInterceptor for /api/**");
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/advocates/login",
                        "/api/advocates/signup",
                        "/api/auth/**",
                        "/api/whatsapp/webhook",
                        "/api/health"
                );
    }

    @Bean
    public CorsFilter corsFilter() {
        log.info("Creating CorsFilter bean...");
        CorsConfiguration config = new CorsConfiguration();

        Set<String> originPatterns = new LinkedHashSet<>();

        if (allowedOrigins != null) {
            for (String origin : allowedOrigins.split(",")) {
                origin = origin.trim();
                if (!origin.isEmpty()) {
                    originPatterns.add(origin);
                }
            }
        }

        originPatterns.add("https://*.vercel.app");
        originPatterns.add("http://localhost:*");

        for (String pattern : originPatterns) {
            config.addAllowedOriginPattern(pattern);
            log.info("  Allowed origin pattern: '{}'", pattern);
        }

        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        log.info("=== CorsFilter Final Configuration ===");
        log.info("  Origin patterns: {}", config.getAllowedOriginPatterns());
        log.info("  Methods: {}", config.getAllowedMethods());
        log.info("  Headers: {}", config.getAllowedHeaders());
        log.info("  Credentials: {}", config.getAllowCredentials());
        log.info("  Max age: {}", config.getMaxAge());

        return new CorsFilter(source);
    }
}
