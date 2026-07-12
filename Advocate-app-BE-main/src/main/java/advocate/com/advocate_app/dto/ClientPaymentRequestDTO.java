package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

public class ClientPaymentRequestDTO {
    @NotNull(message = "Payment amount cannot be null")
    private Double amount;

    private String paymentMode;
    private String referenceNumber;
    private Date paymentDate;
    private String description;

    @NotNull(message = "Case ID cannot be null")
    private Long caseId;

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
}
