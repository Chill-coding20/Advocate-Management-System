package advocate.com.advocate_app.communication.provider;

import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.entity.CommunicationSettings;
import advocate.com.advocate_app.communication.enums.NotificationChannel;
import advocate.com.advocate_app.communication.repository.CommunicationSettingsRepository;
import advocate.com.advocate_app.communication.service.CommunicationCryptoService;
import advocate.com.advocate_app.communication.service.EmailSignatureService;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Component
public class EmailNotificationProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationProvider.class);

    private final CommunicationSettingsRepository settingsRepository;
    private final AdvocateRepository advocateRepository;
    private final CommunicationCryptoService cryptoService;
    private final EmailSignatureService emailSignatureService;
    private final Executor taskExecutor;

    public EmailNotificationProvider(CommunicationSettingsRepository settingsRepository,
                                      AdvocateRepository advocateRepository,
                                      CommunicationCryptoService cryptoService,
                                      EmailSignatureService emailSignatureService,
                                      @Qualifier("notificationTaskExecutor") Executor taskExecutor) {
        this.settingsRepository = settingsRepository;
        this.advocateRepository = advocateRepository;
        this.cryptoService = cryptoService;
        this.emailSignatureService = emailSignatureService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        Instant start = Instant.now();

        Advocate advocate = lookupAdvocate(payload);
        if (advocate == null) {
            log.warn("Advocate not found for email");
            return new NotificationResult(false, null, "Advocate not found");
        }

        CommunicationSettings settings = settingsRepository.findByAdvocate(advocate).orElse(null);
        if (settings == null || !settings.isEmailEnabled()) {
            log.warn("Email is disabled for advocate: {}", advocate.getEmail());
            return new NotificationResult(false, null, "Email is disabled.");
        }

        String validationError = validateSettings(settings);
        if (validationError != null) {
            log.warn("SMTP settings validation failed: {}", validationError);
            return new NotificationResult(false, null, validationError);
        }

        String recipientEmail = payload.getRecipientEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("No recipient email provided");
            return new NotificationResult(false, null, "Recipient email is required.");
        }

        String subject = resolveTemplate(payload.getSubject(), payload.getVariables());
        String htmlBody = buildHtmlBody(payload);
        String decryptedPassword = cryptoService.decrypt(settings.getEncryptedPassword());
        log.debug("Email password decrypted successfully");

        org.springframework.mail.javamail.JavaMailSender mailSender = createMailSender(settings, decryptedPassword);

        CompletableFuture<NotificationResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                sendEmail(mailSender, settings, recipientEmail, subject, htmlBody, payload.getAttachments());
                long elapsed = Duration.between(start, Instant.now()).toMillis();
                log.info("EMAIL SENT | to={} | type={} | duration={}ms", recipientEmail, payload.getType(), elapsed);
                return new NotificationResult(true, "Email sent to " + recipientEmail, null);
            } catch (Exception e) {
                long elapsed = Duration.between(start, Instant.now()).toMillis();
                log.warn("EMAIL FAILED | to={} | type={} | duration={}ms | error={}",
                        recipientEmail, payload.getType(), elapsed, e.getMessage());
                return new NotificationResult(false, null, e.getMessage() != null ? e.getMessage() : "Unknown email error");
            }
        }, taskExecutor);

        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("EMAIL TIMEOUT | to={} | type={}", recipientEmail, payload.getType());
            return new NotificationResult(false, null, "SMTP send timed out after 30 seconds");
        } catch (Exception e) {
            log.warn("EMAIL ASYNC ERROR | to={} | error={}", recipientEmail, e.getMessage());
            return new NotificationResult(false, null, e.getMessage() != null ? e.getMessage() : "Async email error");
        }
    }

    private Advocate lookupAdvocate(NotificationPayload payload) {
        String email = payload.getAdvocateEmail();
        if (email != null && !email.isBlank()) {
            return advocateRepository.findByEmail(email).orElse(null);
        }
        if (payload.getAdvocate() != null) {
            return payload.getAdvocate();
        }
        return null;
    }

    private String validateSettings(CommunicationSettings s) {
        if (s.getSmtpHost() == null || s.getSmtpHost().isBlank()) return "SMTP Host is not configured.";
        if (s.getSmtpPort() == null || s.getSmtpPort() <= 0) return "SMTP Port is not configured.";
        if (s.getSenderEmail() == null || s.getSenderEmail().isBlank()) return "Sender Email is not configured.";
        if (s.getEncryptedPassword() == null || s.getEncryptedPassword().isBlank()) return "SMTP Password is not configured.";
        return null;
    }

    private org.springframework.mail.javamail.JavaMailSender createMailSender(CommunicationSettings s, String password) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        org.springframework.mail.javamail.JavaMailSenderImpl sender = new org.springframework.mail.javamail.JavaMailSenderImpl();
        sender.setHost(s.getSmtpHost());
        sender.setPort(s.getSmtpPort());
        sender.setUsername(s.getSenderEmail());
        sender.setPassword(password);
        sender.setJavaMailProperties(props);
        return sender;
    }

    private void sendEmail(org.springframework.mail.javamail.JavaMailSender mailSender,
                           CommunicationSettings s, String to, String subject, String htmlBody,
                           java.util.List<String> attachmentPaths) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                message, attachmentPaths != null && !attachmentPaths.isEmpty(), "UTF-8");
        helper.setFrom(s.getSenderEmail(), s.getSenderName() != null ? s.getSenderName() : s.getSenderEmail());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        if (attachmentPaths != null) {
            for (String pathStr : attachmentPaths) {
                try {
                    Path path = Path.of(pathStr);
                    if (Files.exists(path) && Files.isRegularFile(path)) {
                        File file = path.toFile();
                        helper.addAttachment(file.getName(), file);
                    } else {
                        log.warn("Attachment not found or not a file: {}", pathStr);
                    }
                } catch (Exception e) {
                    log.warn("Failed to attach file: {} | error: {}", pathStr, e.getMessage());
                }
            }
        }

        mailSender.send(message);
    }

    private String buildHtmlBody(NotificationPayload payload) {
        // Use emailBody if it's a full HTML template (from EmailTemplateService)
        if (payload.getEmailBody() != null && payload.getEmailBody().contains("<!DOCTYPE html>")) {
            Advocate advocate = lookupAdvocate(payload);
            if (advocate != null) {
                return emailSignatureService.appendSignature(payload.getEmailBody(), advocate);
            }
            return payload.getEmailBody();
        }

        String body = payload.getEmailBody() != null ? payload.getEmailBody() : payload.getMessage();
        if (body == null) body = "";

        body = body.replace("\n", "<br/>");

        String recipientName = payload.getRecipientName();
        if (recipientName != null && !recipientName.isBlank()) {
            body = "<p>Dear " + escapeHtml(recipientName) + ",</p>" + body;
        }

        body = resolveTemplate(body, payload.getVariables());

        body = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/></head><body style=\"font-family:Arial,sans-serif;color:#333;padding:20px\">"
                + body
                + "</body></html>";

        Advocate advocate = lookupAdvocate(payload);
        if (advocate != null) {
            body = emailSignatureService.appendSignature(body, advocate);
        }

        return body;
    }

    private String resolveTemplate(String text, Map<String, String> variables) {
        if (text == null) return "";
        if (variables == null) return text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            text = text.replace(key, value);
        }
        text = text.replaceAll("\\{\\{\\w+}}", "");
        return text;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
