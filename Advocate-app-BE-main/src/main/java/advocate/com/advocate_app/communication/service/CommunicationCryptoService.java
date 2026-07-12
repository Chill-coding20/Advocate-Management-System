package advocate.com.advocate_app.communication.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CommunicationCryptoService {

    private static final Logger log = LoggerFactory.getLogger(CommunicationCryptoService.class);
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey secretKey;

    public CommunicationCryptoService(@Value("${app.crypto.secret-key}") String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException(
                "app.crypto.secret-key is not configured. Set a 32-byte Base64-encoded AES-256 key in application.properties. " +
                "Generate one with: java -jar advocate-app.jar --generate-key");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key);
        } catch (Exception e) {
            throw new IllegalStateException("app.crypto.secret-key is not valid Base64: " + e.getMessage(), e);
        }
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                "app.crypto.secret-key must decode to exactly 32 bytes (256-bit AES). Got " + keyBytes.length + " bytes.");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @PostConstruct
    public void verifyKey() {
        String testPlaintext = "test-encryption-verify";
        String encrypted = encrypt(testPlaintext);
        String decrypted = decrypt(encrypted);
        if (!testPlaintext.equals(decrypted)) {
            throw new IllegalStateException("AES-GCM encryption self-test failed — key may be corrupted");
        }
        log.info("AES-256-GCM crypto initialized successfully with fixed secret key");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) return "";
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM);
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
            byte[] combined = new byte[IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(ciphertext, 0, combined, IV_LENGTH, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            return "";
        }
    }

    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isBlank()) return "";
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            if (combined.length < IV_LENGTH) return "";
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, combined, 0, IV_LENGTH);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            byte[] plaintext = cipher.doFinal(combined, IV_LENGTH, combined.length - IV_LENGTH);
            int pwLen = plaintext.length;
            log.debug("Decrypted SMTP password length: {} chars", pwLen);
            return new String(plaintext, "UTF-8");
        } catch (Exception e) {
            log.error("Decryption failed for stored SMTP password — key may have changed since it was saved", e);
            return "";
        }
    }
}
