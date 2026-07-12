package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.DemoWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @Autowired
    private DemoWorkspaceService demoWorkspaceService;

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(demoWorkspaceService.getStatus(email));
    }

    @PostMapping("/load")
    public ResponseEntity<?> loadDemo(@RequestHeader("Authorization") String token) {
        try {
            String email = JwtUtil.extractEmail(token.substring(7));
            Map<String, Object> result = demoWorkspaceService.generate(email);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearDemo(@RequestHeader("Authorization") String token) {
        try {
            String email = JwtUtil.extractEmail(token.substring(7));
            Map<String, Object> result = demoWorkspaceService.clear(email);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
