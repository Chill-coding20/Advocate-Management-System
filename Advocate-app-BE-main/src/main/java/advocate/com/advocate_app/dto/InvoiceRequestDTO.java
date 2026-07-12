package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class InvoiceRequestDTO {
    @NotBlank(message = "Invoice number cannot be blank")
    private String invoiceNumber;

    @NotNull(message = "Invoice amount cannot be null")
    private Double amount;

    private LocalDate invoiceDate;
    private LocalDate dueDate;

    @NotNull(message = "Case ID cannot be null")
    private Long caseId;

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
}
