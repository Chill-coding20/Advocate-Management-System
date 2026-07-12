package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.TimelineEventResponseDTO;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.CaseTimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases/{caseId}/timeline")
public class CaseTimelineController {

    @Autowired
    private CaseTimelineService timelineService;

    @GetMapping
    public ResponseEntity<Page<TimelineEventResponseDTO>> getTimeline(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) List<String> eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                timelineService.getTimeline(caseId, page, size, eventType, dateFrom, dateTo, search)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<TimelineEventResponseDTO>> getAllTimeline(
            @PathVariable Long caseId) {
        return ResponseEntity.ok(timelineService.getAllTimeline(caseId));
    }
}
