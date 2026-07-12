package advocate.com.advocate_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoice_advocate", columnList = "advocate_id"),
    @Index(name = "idx_invoice_case", columnList = "case_id"),
    @Index(name = "idx_invoice_client", columnList = "client_id"),
    @Index(name = "idx_invoice_advocate_status", columnList = "advocate_id, status")
})
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private String status = "UNPAID"; // PAID, UNPAID, OVERDUE

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    @JsonIgnoreProperties({"advocate", "client"})
    private CaseEntity caseEntity;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties({"cases"})
    private Client client;

    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = false)
    @JsonIgnoreProperties({"password", "cases"})
    private Advocate advocate;

    // ----- Getters & Setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public CaseEntity getCaseEntity() { return caseEntity; }
    public void setCaseEntity(CaseEntity caseEntity) { this.caseEntity = caseEntity; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}
