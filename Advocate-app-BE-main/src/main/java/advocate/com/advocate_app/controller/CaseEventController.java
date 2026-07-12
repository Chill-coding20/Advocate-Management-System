package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.CaseEventRequestDTO;
import advocate.com.advocate_app.dto.CaseEventResponseDTO;
import advocate.com.advocate_app.entity.CaseEventEntity;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.CaseEventMapper;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.CaseEventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class CaseEventController {

    @Autowired
    private CaseEventService caseEventService;

    @Autowired
    private CaseEventMapper caseEventMapper;

    @PostMapping("/create")
    @RequirePermission("EVENT_CREATE")
    public ResponseEntity<CaseEventResponseDTO> createEvent(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CaseEventRequestDTO requestDTO) {
        String email = JwtUtil.extractEmail(token.substring(7));
        CaseEventEntity event = caseEventMapper.toEntity(requestDTO);
        // Set caseEntity stub so CaseEventService can resolve it
        advocate.com.advocate_app.entity.CaseEntity caseStub = new advocate.com.advocate_app.entity.CaseEntity();
        if (requestDTO.getCaseEntity() != null) {
            caseStub.setId(requestDTO.getCaseEntity().getId());
        }
        event.setCaseEntity(caseStub);
        CaseEventEntity created = caseEventService.createEvent(email, event);
        return ResponseEntity.status(HttpStatus.CREATED).body(caseEventMapper.toResponseDTO(created));
    }

    @GetMapping
    @RequirePermission("EVENT_VIEW")
    public ResponseEntity<Map<String, Object>> getEventsPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<CaseEventEntity> eventPage = caseEventService.getEventsPaged(email, pageable);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", eventPage.getContent().stream().map(caseEventMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", eventPage.getNumber());
        response.put("size", eventPage.getSize());
        response.put("totalElements", eventPage.getTotalElements());
        response.put("totalPages", eventPage.getTotalPages());
        response.put("hasNext", eventPage.hasNext());
        response.put("hasPrevious", eventPage.hasPrevious());
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/my-events", "/today", "/upcoming"})
    @RequirePermission("EVENT_VIEW")
    public ResponseEntity<List<CaseEventResponseDTO>> getMyEvents(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<CaseEventEntity> events = caseEventService.getMyEvents(email);
        List<CaseEventResponseDTO> dtos = events.stream()
                .map(caseEventMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }



    @DeleteMapping("/delete/{id}")
    @RequirePermission("EVENT_DELETE")
    public ResponseEntity<Void> deleteEvent(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        caseEventService.deleteEvent(email, id);
        return ResponseEntity.noContent().build();
    }
}
