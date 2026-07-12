package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.CaseRequestDTO;
import advocate.com.advocate_app.dto.CaseResponseDTO;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.CaseMapper;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.CaseService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;
    private final CaseMapper caseMapper;

    @Autowired
    public CaseController(CaseService caseService, CaseMapper caseMapper) {
        this.caseService = caseService;
        this.caseMapper = caseMapper;
    }

    @PostMapping("/create")
    @RequirePermission("CASE_CREATE")
    public ResponseEntity<CaseResponseDTO> createCase(@RequestHeader("Authorization") String token,
                                                      @Valid @RequestBody CaseRequestDTO caseDto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        CaseEntity caseEntity = caseMapper.toEntity(caseDto);
        if (caseDto.getClientId() != null) {
            Client client = new Client();
            client.setId(caseDto.getClientId());
            caseEntity.setClient(client);
        }
        CaseEntity savedCase = caseService.createCase(email, caseEntity);
        return ResponseEntity.ok(caseMapper.toResponseDTO(savedCase));
    }

    @GetMapping
    @RequirePermission("CASE_VIEW")
    public ResponseEntity<Map<String, Object>> getCasesPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "false") boolean archived) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CaseEntity> casePage;
        if (archived) {
            casePage = keyword != null && !keyword.isBlank()
                    ? caseService.searchArchivedCasesPaged(email, keyword, pageable)
                    : caseService.getArchivedCasesPaged(email, pageable);
        } else {
            casePage = keyword != null && !keyword.isBlank()
                    ? caseService.searchCasesPaged(email, keyword, pageable)
                    : caseService.getCasesPaged(email, pageable);
        }
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", casePage.getContent().stream().map(caseMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", casePage.getNumber());
        response.put("size", casePage.getSize());
        response.put("totalElements", casePage.getTotalElements());
        response.put("totalPages", casePage.getTotalPages());
        response.put("hasNext", casePage.hasNext());
        response.put("hasPrevious", casePage.hasPrevious());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-cases")
    @RequirePermission("CASE_VIEW")
    public ResponseEntity<List<CaseResponseDTO>> getMyCases(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<CaseResponseDTO> cases = caseService.getMyCases(email).stream()
                .map(caseMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/search")
    @RequirePermission("CASE_VIEW")
    public ResponseEntity<List<CaseResponseDTO>> searchCases(@RequestHeader("Authorization") String token,
                                                             @RequestParam(required = false) String keyword) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<CaseResponseDTO> cases = caseService.searchCases(email, keyword).stream()
                .map(caseMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cases);
    }

    @PutMapping("/update/{id}")
    @RequirePermission("CASE_EDIT")
    public ResponseEntity<CaseResponseDTO> updateCase(@RequestHeader("Authorization") String token,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody CaseRequestDTO caseDto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        CaseEntity updatedEntity = caseMapper.toEntity(caseDto);
        if (caseDto.getClientId() != null) {
            Client client = new Client();
            client.setId(caseDto.getClientId());
            updatedEntity.setClient(client);
        }
        CaseEntity savedCase = caseService.updateCase(email, id, updatedEntity);
        return ResponseEntity.ok(caseMapper.toResponseDTO(savedCase));
    }

    @DeleteMapping("/delete/{id}")
    @RequirePermission("CASE_DELETE")
    public ResponseEntity<String> deleteCase(@RequestHeader("Authorization") String token,
                                              @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        caseService.deleteCase(email, id);
        return ResponseEntity.ok("Case archived successfully");
    }

    @PutMapping("/restore/{id}")
    @RequirePermission("CASE_EDIT")
    public ResponseEntity<String> restoreCase(@RequestHeader("Authorization") String token,
                                               @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        caseService.restoreCase(email, id);
        return ResponseEntity.ok("Case restored successfully");
    }
}
