package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.TaskRequestDTO;
import advocate.com.advocate_app.dto.TaskResponseDTO;
import advocate.com.advocate_app.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponseDTO toResponseDTO(Task task) {
        if (task == null) return null;
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setPriority(task.getPriority());
        dto.setDeadline(task.getDeadline());
        dto.setCompleted(task.isCompleted());
        return dto;
    }

    public Task toEntity(TaskRequestDTO dto) {
        if (dto == null) return null;
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setPriority(dto.getPriority());
        task.setDeadline(dto.getDeadline());
        task.setCompleted(false);
        return task;
    }

    public void updateEntityFromRequestDTO(TaskRequestDTO dto, Task task) {
        if (dto == null || task == null) return;
        task.setTitle(dto.getTitle());
        task.setPriority(dto.getPriority());
        task.setDeadline(dto.getDeadline());
    }
}
