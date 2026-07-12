package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Task;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    public Page<Task> getTasksPaged(Advocate advocate, Pageable pageable) {
        return taskRepository.findByAdvocate(advocate, pageable);
    }

    public List<Task> getMyTasks(Advocate advocate) {
        return taskRepository.findByAdvocateOrderByCompletedAscDeadlineAsc(advocate);
    }

    public Task createTask(String email, Task task) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found."));
        task.setAdvocate(advocate);
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    public Task toggleTask(Long id, String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found."));
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found."));
        if (!task.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to modify this task");
        }
        task.setCompleted(!task.isCompleted());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id, String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found."));
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found."));
        if (!task.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to delete this task");
        }
        taskRepository.delete(task);
    }
}
