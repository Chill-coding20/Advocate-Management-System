package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.SearchResponseDTO;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchResponseDTO> search(
            @RequestParam("q") String query,
            @RequestHeader("Authorization") String token) {

        String email = JwtUtil.extractEmail(token.substring(7));
        log.info("Search request - keyword: \"{}\", email: {}", query, email);

        SearchResponseDTO results = searchService.search(email, query.trim());
        log.info("Search response size - total items across all modules");

        return ResponseEntity.ok(results);
    }

    @GetMapping("/global")
    public ResponseEntity<SearchResponseDTO> globalSearch(
            @RequestParam("q") String query,
            @RequestHeader("Authorization") String token) {

        String email = JwtUtil.extractEmail(token.substring(7));
        log.info("Global search request - keyword: \"{}\", email: {}", query, email);

        SearchResponseDTO results = searchService.search(email, query.trim());

        return ResponseEntity.ok(results);
    }
}
