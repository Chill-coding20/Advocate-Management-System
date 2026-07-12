package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CaseRequestDTO {
    @NotBlank(message = "Case number cannot be blank")
    @Size(min = 16, max = 16, message = "Case number must be exactly 16 digits")
    private String caseNumber;

    @NotBlank(message = "Case title cannot be blank")
    private String caseTitle;

    private String caseType;
    private String courtLevel;
    private String status;
    private Double amount;
    private String description;

    @NotNull(message = "Client ID cannot be null")
    private Long clientId;

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getCaseTitle() { return caseTitle; }
    public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }

    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }

    public String getCourtLevel() { return courtLevel; }
    public void setCourtLevel(String courtLevel) { this.courtLevel = courtLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
}
