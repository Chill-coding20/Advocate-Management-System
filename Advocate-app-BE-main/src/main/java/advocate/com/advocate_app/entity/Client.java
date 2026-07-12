package advocate.com.advocate_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "clients", indexes = {
    @Index(name = "idx_client_advocate", columnList = "advocate_id"),
    @Index(name = "idx_client_email", columnList = "email"),
    @Index(name = "idx_client_advocate_deleted", columnList = "advocate_id, deleted"),
    @Index(name = "idx_client_advocate_created", columnList = "advocate_id, created_at")
})
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String address;

    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDate createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDate.now();
    }

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"client", "advocate"})
    private List<CaseEntity> cases;

    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = true)
    @JsonIgnoreProperties({"password", "cases", "clients"})
    private Advocate advocate;

    // ----- Getters & Setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public List<CaseEntity> getCases() { return cases; }
    public void setCases(List<CaseEntity> cases) { this.cases = cases; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}

