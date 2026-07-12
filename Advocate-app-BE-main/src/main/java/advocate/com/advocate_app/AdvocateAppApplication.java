package advocate.com.advocate_app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdvocateAppApplication {
    private static final Logger log = LoggerFactory.getLogger(AdvocateAppApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AdvocateAppApplication.class, args);
        log.info("Advocate Application Backend Started Successfully!");
    }

    @Bean
    public CommandLineRunner environmentCheck(ConfigurableEnvironment env) {
        return args -> {
            String jwtSecret = env.getProperty("app.jwt.secret");
            String cryptoKey = env.getProperty("app.crypto.secret-key");
            String dbUrl = env.getProperty("spring.datasource.url");

            if (jwtSecret == null || jwtSecret.isBlank()) {
                log.error("FATAL: JWT_SECRET is not configured. Set app.jwt.secret or JWT_SECRET env var.");
                System.exit(1);
            }
            if (dbUrl == null || dbUrl.isBlank()) {
                log.error("FATAL: DB_URL is not configured. Set spring.datasource.url or DB_URL env var.");
                System.exit(1);
            }
            if (jwtSecret.equals("my_super_secret_key_for_advocate_app_12345")) {
                log.warn("JWT_SECRET is using the default development value. Set JWT_SECRET env var to a strong secret in production.");
            }
            if (cryptoKey == null || cryptoKey.equals("lcoOTkTTutUh+fREXKXflg540Sk4zndpgHNDPh9dtj4=")) {
                log.warn("CRYPTO_SECRET_KEY is using the default development value. Set CRYPTO_SECRET_KEY env var in production.");
            }
        };
    }


}
