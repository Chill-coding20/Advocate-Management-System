package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

public class ExpenseRequestDTO {
    @NotBlank(message = "Expense title cannot be blank")
    private String title;

    @NotNull(message = "Expense amount cannot be null")
    private Double amount;

    private String category;
    private String description;
    private String paymentMode;
    private String paymentStatus;
    private String referenceNumber;
    private Date paymentDate;
    private Long caseId;
    private String expenseType;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }
}
