package advocate.com.advocate_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities", indexes = {
    @Index(name = "idx_activity_advocate", columnList = "advocate_id"),
    @Index(name = "idx_activity_advocate_timestamp", columnList = "advocate_id, timestamp")
})
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private String actionType; // CASE_CREATED, HEARING_UPDATED, INVOICE_CREATED, DOCUMENT_UPLOADED, LOGIN

    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = false)
    @JsonIgnoreProperties({"password", "cases"})
    private Advocate advocate;

    // ----- Constructors -----
    public Activity() {}

    public Activity(String description, String actionType, Advocate advocate) {
        this.description = description;
        this.actionType = actionType;
        this.advocate = advocate;
        this.timestamp = LocalDateTime.now();
    }

    // ----- Getters & Setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}
