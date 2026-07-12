package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.ClientPaymentRequestDTO;
import advocate.com.advocate_app.dto.ClientPaymentResponseDTO;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.ClientPayment;
import advocate.com.advocate_app.mapper.ClientPaymentMapper;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.ClientPaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class ClientPaymentController {

    @Autowired
    private ClientPaymentService paymentService;

    @Autowired
    private ClientPaymentMapper paymentMapper;

    @GetMapping
    @RequirePermission("PAYMENT_VIEW")
    public ResponseEntity<Map<String, Object>> getPaymentsPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ClientPayment> paymentPage = paymentService.getPaymentsPaged(email, pageable);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", paymentPage.getContent().stream().map(paymentMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", paymentPage.getNumber());
        response.put("size", paymentPage.getSize());
        response.put("totalElements", paymentPage.getTotalElements());
        response.put("totalPages", paymentPage.getTotalPages());
        response.put("hasNext", paymentPage.hasNext());
        response.put("hasPrevious", paymentPage.hasPrevious());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @RequirePermission("PAYMENT_CREATE")
    public ResponseEntity<ClientPaymentResponseDTO> createPayment(@RequestHeader("Authorization") String token, 
                                                                  @Valid @RequestBody ClientPaymentRequestDTO paymentDto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        ClientPayment payment = paymentMapper.toEntity(paymentDto);
        if (paymentDto.getCaseId() != null) {
            CaseEntity caseEntity = new CaseEntity();
            caseEntity.setId(paymentDto.getCaseId());
            payment.setCaseEntity(caseEntity);
        }
        ClientPayment saved = paymentService.createPayment(email, payment);
        return ResponseEntity.ok(paymentMapper.toResponseDTO(saved));
    }

    @GetMapping("/case/{caseId}")
    @RequirePermission("PAYMENT_VIEW")
    public ResponseEntity<List<ClientPaymentResponseDTO>> getCasePayments(@RequestHeader("Authorization") String token, 
                                                                          @PathVariable Long caseId) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<ClientPaymentResponseDTO> payments = paymentService.getPaymentsByCase(email, caseId).stream()
                .map(paymentMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/today")
    @RequirePermission("PAYMENT_VIEW")
    public ResponseEntity<Map<String, Object>> getTodayPayments(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Map<String, Object> report = paymentService.getTodaySummary(email);
        if (report.containsKey("payments")) {
            List<?> paymentsList = (List<?>) report.get("payments");
            List<ClientPaymentResponseDTO> dtos = paymentsList.stream()
                    .map(p -> paymentMapper.toResponseDTO((ClientPayment) p))
                    .collect(Collectors.toList());
            Map<String, Object> cleanReport = new HashMap<>(report);
            cleanReport.put("payments", dtos);
            return ResponseEntity.ok(cleanReport);
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/monthly")
    @RequirePermission("PAYMENT_VIEW")
    public ResponseEntity<Map<String, Object>> getMonthlyPayments(@RequestHeader("Authorization") String token,
                                                                   @RequestParam int year,
                                                                   @RequestParam int month) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Map<String, Object> report = paymentService.getMonthlySummary(email, year, month);
        if (report.containsKey("payments")) {
            List<?> paymentsList = (List<?>) report.get("payments");
            List<ClientPaymentResponseDTO> dtos = paymentsList.stream()
                    .map(p -> paymentMapper.toResponseDTO((ClientPayment) p))
                    .collect(Collectors.toList());
            Map<String, Object> cleanReport = new HashMap<>(report);
            cleanReport.put("payments", dtos);
            return ResponseEntity.ok(cleanReport);
        }
        return ResponseEntity.ok(report);
    }


}
