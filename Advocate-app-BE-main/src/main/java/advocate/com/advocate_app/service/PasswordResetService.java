package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.PasswordResetOtp;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.PasswordResetOtpRepository;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${app.otp.rate-limit-per-hour:5}")
    private int rateLimitPerHour;

    @Value("${app.otp.salt}")
    private String otpSalt;

    @Autowired
    private PasswordResetOtpRepository otpRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String hashOtp(String otp) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((otp + otpSalt).getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash OTP", e);
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public void requestOtp(String email) {
        try {
            Optional<Advocate> advocateOpt = advocateRepository.findByEmail(email);

            if (advocateOpt.isEmpty()) {
                log.info("OTP requested for unknown email: {}", email);
                return;
            }

            Advocate advocate = advocateOpt.get();

            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentCount = otpRepository.countByEmailAndCreatedAtAfter(email, oneHourAgo);
            if (recentCount >= rateLimitPerHour) {
                log.warn("Rate limit exceeded for email: {}", email);
                return;
            }

            String otp = generateOtp();
            String hashed = hashOtp(otp);

            PasswordResetOtp otpEntity = new PasswordResetOtp();
            otpEntity.setAdvocateId(advocate.getId());
            otpEntity.setEmail(email);
            otpEntity.setHashedOtp(hashed);
            otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
            otpEntity.setUsed(false);
            otpRepository.save(otpEntity);

            sendOtpEmail(advocate.getEmail(), advocate.getFullName(), otp);
            log.info("OTP sent to email: {}", email);
        } catch (Exception e) {
            log.error("Error processing OTP request for {}: {}", email, e.getMessage());
        }
    }

    private void sendOtpEmail(String to, String name, String otp) {
        try {
            String subject = "Password Reset Request – Advocate App";
            String htmlBody = emailTemplateService.otpEmail(name, otp, otpExpiryMinutes);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<PasswordResetOtp> latestOpt = otpRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (latestOpt.isEmpty()) {
            return false;
        }

        PasswordResetOtp otpEntity = latestOpt.get();
        if (otpEntity.isUsed()) {
            return false;
        }
        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        String hashed = hashOtp(otp);
        return hashed.equals(otpEntity.getHashedOtp());
    }

    @Transactional
    public boolean resetPassword(String email, String otp, String newPassword) {
        if (!verifyOtp(email, otp)) {
            return false;
        }

        Optional<PasswordResetOtp> latestOpt = otpRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (latestOpt.isEmpty()) {
            return false;
        }

        PasswordResetOtp otpEntity = latestOpt.get();
        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);

        Optional<Advocate> advocateOpt = advocateRepository.findByEmail(email);
        if (advocateOpt.isEmpty()) {
            return false;
        }

        Advocate advocate = advocateOpt.get();
        advocate.setPassword(passwordEncoder.encode(newPassword));
        advocateRepository.save(advocate);

        otpRepository.deleteByEmail(email);
        log.info("Password reset successful for email: {}", email);
        return true;
    }
}
