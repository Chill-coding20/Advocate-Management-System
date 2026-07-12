package advocate.com.advocate_app.dto;

import java.util.Date;

public class ClientPaymentResponseDTO {
    private Long id;
    private Double amount;
    private String paymentMode;
    private String referenceNumber;
    private Date paymentDate;
    private String description;
    private Long caseId;
    private String caseTitle;
    private Long clientId;
    private String clientName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCaseTitle() { return caseTitle; }
    public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
}
