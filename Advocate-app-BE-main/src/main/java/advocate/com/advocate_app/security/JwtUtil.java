package advocate.com.advocate_app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static String secret;
    private static long expirationTime;
    private static Key key;

    @Value("${app.jwt.secret}")
    private String injectedSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long injectedExpiration;

    @PostConstruct
    public void init() {
        if (injectedSecret == null || injectedSecret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET is not configured. Set app.jwt.secret in application.properties " +
                    "or JWT_SECRET environment variable."
            );
        }
        secret = injectedSecret;
        expirationTime = injectedExpiration;
        key = new SecretKeySpec(
                Base64.getDecoder().decode(Base64.getEncoder().encodeToString(secret.getBytes())),
                SignatureAlgorithm.HS256.getJcaName()
        );
    }

    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    public static String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
