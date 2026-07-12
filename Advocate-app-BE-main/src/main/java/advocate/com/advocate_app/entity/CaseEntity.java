package advocate.com.advocate_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cases", indexes = {
    @Index(name = "idx_case_advocate", columnList = "advocate_id"),
    @Index(name = "idx_case_client", columnList = "client_id"),
    @Index(name = "idx_case_advocate_status", columnList = "advocate_id, status"),
    @Index(name = "idx_case_advocate_deleted", columnList = "advocate_id, deleted"),
    @Index(name = "idx_case_advocate_created", columnList = "advocate_id, created_at"),
    @Index(name = "idx_case_advocate_court_status", columnList = "advocate_id, court_level, status")
})
public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String caseNumber;

    private String caseTitle;
    private String caseType;
    private String courtLevel;
    private String status;
    private Double amount;
    private String description;
    private Double estimatedAmount;

    // ✅ NEW FIELDS for expense and payment tracking
    private Double totalClientAgreedAmount;   // Total cost agreed with client
    private Double totalPaidByClient;         // How much client has paid so far
    private Double totalExpensesSoFar;        // Auto-calculated sum of expenses
    private Double balanceInAccount;          // Paid - Expenses
    private Double pendingFromClient;         // Agreed - Paid

    // ✅ Soft Delete Field
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    @Column(updatable = false)
    private java.time.LocalDate createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = java.time.LocalDate.now();
        }
    }

    // Each case belongs to an Advocate
    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = false)
    @JsonIgnoreProperties({"password", "cases"})
    private Advocate advocate;

    // Each case may belong to a Client (optional)
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    @JsonIgnoreProperties({"cases"})
    private Client client;

    // ------------------ MANUAL GETTERS AND SETTERS ------------------

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

    public Double getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(Double estimatedAmount) { this.estimatedAmount = estimatedAmount; }

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

    public java.time.LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDate createdAt) { this.createdAt = createdAt; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}
