package advocate.com.advocate_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_expense_advocate", columnList = "advocate_id"),
    @Index(name = "idx_expense_case", columnList = "case_id"),
    @Index(name = "idx_expense_client", columnList = "client_id"),
    @Index(name = "idx_expense_advocate_paydate", columnList = "advocate_id, payment_date")
})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // e.g., "Court Fee"

    private String category; // e.g., "Travel", "Documentation"
    private Double amount;

    private String paymentMode;   // Cash, UPI, Bank Transfer
    private String paymentStatus; // Paid, Pending, Reimbursed
    private String referenceNumber; // transaction id, cheque no etc.

    @Temporal(TemporalType.DATE)
    private Date paymentDate;

    private String description;

    // ✅ Expense type — CLIENT_CASE or GENERAL
    @Column(nullable = false)
    private String expenseType = "CLIENT_CASE";

    // ✅ Linked Case (for case-related expenses)
    @ManyToOne
    @JoinColumn(name = "case_id")
    @JsonIgnoreProperties({"advocate", "client"})
    private CaseEntity caseEntity;

    // ✅ Auto-linked Client from Case
    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"cases"})
    private Client client;

    // ✅ Advocate who created this expense
    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = false)
    @JsonIgnoreProperties({"password", "cases"})
    private Advocate advocate;

    // ---------- GETTERS & SETTERS ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}
