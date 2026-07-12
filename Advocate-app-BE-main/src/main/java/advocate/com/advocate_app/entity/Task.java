package advocate.com.advocate_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_advocate", columnList = "advocate_id"),
    @Index(name = "idx_task_advocate_comp_deadline", columnList = "advocate_id, completed, deadline")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String priority = "MEDIUM"; // HIGH, MEDIUM, LOW

    private LocalDate deadline;

    @Column(nullable = false)
    private boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "advocate_id", nullable = false)
    @JsonIgnoreProperties({"password", "cases"})
    private Advocate advocate;

    // ----- Getters & Setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Advocate getAdvocate() { return advocate; }
    public void setAdvocate(Advocate advocate) { this.advocate = advocate; }
}
