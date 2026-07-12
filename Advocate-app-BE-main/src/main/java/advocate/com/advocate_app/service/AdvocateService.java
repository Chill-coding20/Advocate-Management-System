package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdvocateService {

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Advocate registerUser(Advocate advocate) {
        advocate.setPassword(passwordEncoder.encode(advocate.getPassword()));
        return advocateRepository.save(advocate);
    }

    public boolean checkLogin(String email, String password) {
        return advocateRepository.findByEmail(email)
                .map(advocate -> checkPasswordAndUpgrade(password, advocate))
                .orElse(false);
    }

    private boolean checkPasswordAndUpgrade(String rawPassword, Advocate advocate) {
        String stored = advocate.getPassword();

        if (isBcryptHash(stored)) {
            if (passwordEncoder.matches(rawPassword, stored)) {
                return true;
            }
        } else {
            if (stored.equals(rawPassword)) {
                advocate.setPassword(passwordEncoder.encode(rawPassword));
                advocateRepository.save(advocate);
                return true;
            }
        }
        return false;
    }

    public static boolean isBcryptHash(String password) {
        return password != null &&
                (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}
