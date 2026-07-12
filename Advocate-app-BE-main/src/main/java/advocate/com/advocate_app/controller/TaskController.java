package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.TaskRequestDTO;
import advocate.com.advocate_app.dto.TaskResponseDTO;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Task;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.TaskMapper;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private TaskMapper taskMapper;

    @GetMapping
    @RequirePermission("TASK_VIEW")
    public ResponseEntity<Map<String, Object>> getTasksPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("deadline").ascending());
        Page<Task> taskPage = taskService.getTasksPaged(advocate, pageable);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", taskPage.getContent().stream().map(taskMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", taskPage.getNumber());
        response.put("size", taskPage.getSize());
        response.put("totalElements", taskPage.getTotalElements());
        response.put("totalPages", taskPage.getTotalPages());
        response.put("hasNext", taskPage.hasNext());
        response.put("hasPrevious", taskPage.hasPrevious());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-tasks")
    @RequirePermission("TASK_VIEW")
    public ResponseEntity<List<TaskResponseDTO>> getMyTasks(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        List<Task> tasks = taskService.getMyTasks(advocate);
        List<TaskResponseDTO> dtos = tasks.stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/create")
    @RequirePermission("TASK_CREATE")
    public ResponseEntity<TaskResponseDTO> createTask(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody TaskRequestDTO requestDTO) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Task task = taskMapper.toEntity(requestDTO);
        Task created = taskService.createTask(email, task);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toResponseDTO(created));
    }

    @PutMapping("/toggle/{id}")
    @RequirePermission("TASK_EDIT")
    public ResponseEntity<TaskResponseDTO> toggleTask(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Task updated = taskService.toggleTask(id, email);
        return ResponseEntity.ok(taskMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/delete/{id}")
    @RequirePermission("TASK_DELETE")
    public ResponseEntity<Void> deleteTask(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        taskService.deleteTask(id, email);
        return ResponseEntity.noContent().build();
    }
}
