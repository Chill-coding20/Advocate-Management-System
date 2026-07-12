package advocate.com.advocate_app.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class TaskRequestDTO {
    @NotBlank(message = "Task title cannot be blank")
    private String title;

    private String description;
    private LocalDate deadline;
    private String priority;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
