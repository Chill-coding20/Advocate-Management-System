package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.AssistantRequest;
import advocate.com.advocate_app.dto.AssistantResponse;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.AssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @PostMapping("/query")
    public ResponseEntity<AssistantResponse> query(@RequestHeader("Authorization") String token,
                                                    @RequestBody AssistantRequest request) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));

        AssistantResponse response = assistantService.processQuery(
                request.getQuery(),
                request.getCurrentRoute(),
                advocate
        );
        return ResponseEntity.ok(response);
    }
}
