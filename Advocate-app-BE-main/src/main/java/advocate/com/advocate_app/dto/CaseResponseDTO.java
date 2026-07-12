package advocate.com.advocate_app.dto;

public class CaseResponseDTO {
    private Long id;
    private String caseNumber;
    private String caseTitle;
    private String caseType;
    private String courtLevel;
    private String status;
    private Double amount;
    private String description;
    private Double totalClientAgreedAmount;
    private Double totalPaidByClient;
    private Double totalExpensesSoFar;
    private Double balanceInAccount;
    private Double pendingFromClient;
    private boolean deleted;
    private Long clientId;
    private String clientName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Double getTotalClientAgreedAmount() { return totalClientAgreedAmount; }
    public void setTotalClientAgreedAmount(Double totalClientAgreedAmount) { this.totalClientAgreedAmount = totalClientAgreedAmount; }

    public Double getTotalPaidByClient() { return totalPaidByClient; }
    public void setTotalPaidByClient(Double totalPaidByClient) { this.totalPaidByClient = totalPaidByClient; }

    public Double getTotalExpensesSoFar() { return totalExpensesSoFar; }
    public void setTotalExpensesSoFar(Double totalExpensesSoFar) { this.totalExpensesSoFar = totalExpensesSoFar; }

    public Double getBalanceInAccount() { return balanceInAccount; }
    public void setBalanceInAccount(Double balanceInAccount) { this.balanceInAccount = balanceInAccount; }

    public Double getPendingFromClient() { return pendingFromClient; }
    public void setPendingFromClient(Double pendingFromClient) { this.pendingFromClient = pendingFromClient; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
}
