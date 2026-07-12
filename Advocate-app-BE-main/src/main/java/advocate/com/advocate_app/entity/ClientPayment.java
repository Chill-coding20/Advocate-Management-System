package advocate.com.advocate_app.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "client_payments", indexes = {
    @Index(name = "idx_cp_advocate", columnList = "advocate_id"),
    @Index(name = "idx_cp_case", columnList = "case_id"),
    @Index(name = "idx_cp_client", columnList = "client_id"),
    @Index(name = "idx_cp_advocate_paydate", columnList = "advocate_id, payment_date"),
    @Index(name = "idx_cp_paydate", columnList = "payment_date")
})
public class ClientPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private String paymentMode;
    private String referenceNumber;

    @Temporal(TemporalType.DATE)
    private Date paymentDate;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private CaseEntity caseEntity;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "advocate_id")
    private Advocate advocate;

    // ---------------- Constructors ----------------
    public ClientPayment() {
    }

    public ClientPayment(Long id, Double amount, String paymentMode, String referenceNumber,
                         Date paymentDate, String description,
                         CaseEntity caseEntity, Client client, Advocate advocate) {
        this.id = id;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.referenceNumber = referenceNumber;
        this.paymentDate = paymentDate;
        this.description = description;
        this.caseEntity = caseEntity;
        this.client = client;
        this.advocate = advocate;
    }

    // ---------------- Getters ----------------
    public Long getId() {
        return id;
    }

    public Double getAmount() {
        return amount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public String getDescription() {
        return description;
    }

    public CaseEntity getCaseEntity() {
        return caseEntity;
    }

    public Client getClient() {
        return client;
    }

    public Advocate getAdvocate() {
        return advocate;
    }

    // ---------------- Setters ----------------
    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCaseEntity(CaseEntity caseEntity) {
        this.caseEntity = caseEntity;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setAdvocate(Advocate advocate) {
        this.advocate = advocate;
    }

    // ---------------- ToString ----------------
    @Override
    public String toString() {
        return "ClientPayment{" +
                "id=" + id +
                ", amount=" + amount +
                ", paymentMode='" + paymentMode + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", paymentDate=" + paymentDate +
                ", description='" + description + '\'' +
                ", caseEntity=" + (caseEntity != null ? caseEntity.getId() : null) +
                ", client=" + (client != null ? client.getId() : null) +
                ", advocate=" + (advocate != null ? advocate.getId() : null) +
                '}';
    }
}
