package advocate.com.advocate_app.communication.entity;

import advocate.com.advocate_app.entity.Advocate;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "communication_settings", indexes = {
    @Index(name = "idx_commsettings_advocate", columnList = "advocate_id")
})
public class CommunicationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advocate_id", nullable = false, unique = true)
    private Advocate advocate;

    @Column(nullable = false)
    private boolean emailEnabled = true;

    @Column(nullable = false)
    private boolean whatsappEnabled = true;

    @Column(length = 255)
    private String smtpHost;

    private Integer smtpPort;

    @Column(length = 255)
    private String senderEmail;

    @Column(length = 255)
    private String senderName;

    @Column(length = 512)
    private String encryptedPassword;

    @Column(length = 255)
    private String whatsappPhoneNumberId;

    @Column(length = 255)
    private String whatsappBusinessAccountId;

    @Column(length = 512)
    private String whatsappAccessToken;

    @Column(length = 255)
    private String replyToEmail;

    @Column(columnDefinition = "TEXT")
    private String emailSignature;

    @Column(nullable = false)
    private int maxRetryCount = 4;

    @Column(nullable = false)
    private int retryDelayMinutes = 2;

    @Column(nullable = false)
    private boolean queueEnabled = true;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String officeAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public CommunicationSettings() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    public boolean isWhatsappEnabled() { return whatsappEnabled; }
    public void setWhatsappEnabled(boolean whatsappEnabled) { this.whatsappEnabled = whatsappEnabled; }
    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    public String getWhatsappPhoneNumberId() { return whatsappPhoneNumberId; }
    public void setWhatsappPhoneNumberId(String whatsappPhoneNumberId) { this.whatsappPhoneNumberId = whatsappPhoneNumberId; }
    public String getWhatsappBusinessAccountId() { return whatsappBusinessAccountId; }
    public void setWhatsappBusinessAccountId(String whatsappBusinessAccountId) { this.whatsappBusinessAccountId = whatsappBusinessAccountId; }
    public String getWhatsappAccessToken() { return whatsappAccessToken; }
    public void setWhatsappAccessToken(String whatsappAccessToken) { this.whatsappAccessToken = whatsappAccessToken; }
    public String getReplyToEmail() { return replyToEmail; }
    public void setReplyToEmail(String replyToEmail) { this.replyToEmail = replyToEmail; }
    public String getEmailSignature() { return emailSignature; }
    public void setEmailSignature(String emailSignature) { this.emailSignature = emailSignature; }
    public int getMaxRetryCount() { return maxRetryCount; }
    public void setMaxRetryCount(int maxRetryCount) { this.maxRetryCount = maxRetryCount; }
    public int getRetryDelayMinutes() { return retryDelayMinutes; }
    public void setRetryDelayMinutes(int retryDelayMinutes) { this.retryDelayMinutes = retryDelayMinutes; }
    public boolean isQueueEnabled() { return queueEnabled; }
    public void setQueueEnabled(boolean queueEnabled) { this.queueEnabled = queueEnabled; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getOfficeAddress() { return officeAddress; }
    public void setOfficeAddress(String officeAddress) { this.officeAddress = officeAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
