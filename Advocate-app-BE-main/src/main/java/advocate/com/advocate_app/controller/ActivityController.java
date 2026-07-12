package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.ActivityResponseDTO;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Activity;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.ActivityMapper;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private ActivityMapper activityMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getActivitiesPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Activity> activityPage = activityService.getActivitiesPaged(advocate, pageable);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", activityPage.getContent().stream().map(activityMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", activityPage.getNumber());
        response.put("size", activityPage.getSize());
        response.put("totalElements", activityPage.getTotalElements());
        response.put("totalPages", activityPage.getTotalPages());
        response.put("hasNext", activityPage.hasNext());
        response.put("hasPrevious", activityPage.hasPrevious());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-activities")
    public ResponseEntity<List<ActivityResponseDTO>> getMyActivities(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        List<Activity> activities = activityService.getRecentActivities(advocate);
        List<ActivityResponseDTO> dtos = activities.stream()
                .map(activityMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
