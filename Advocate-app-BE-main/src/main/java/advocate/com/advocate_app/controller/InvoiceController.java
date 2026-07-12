package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.InvoiceRequestDTO;
import advocate.com.advocate_app.dto.InvoiceResponseDTO;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Invoice;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.InvoiceMapper;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.InvoiceService;
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
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private InvoiceMapper invoiceMapper;

    @GetMapping
    @RequirePermission("INVOICE_VIEW")
    public ResponseEntity<Map<String, Object>> getInvoicesPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Invoice> invoicePage = invoiceService.getInvoicesPaged(email, pageable);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", invoicePage.getContent().stream().map(invoiceMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", invoicePage.getNumber());
        response.put("size", invoicePage.getSize());
        response.put("totalElements", invoicePage.getTotalElements());
        response.put("totalPages", invoicePage.getTotalPages());
        response.put("hasNext", invoicePage.hasNext());
        response.put("hasPrevious", invoicePage.hasPrevious());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-invoices")
    @RequirePermission("INVOICE_VIEW")
    public ResponseEntity<List<InvoiceResponseDTO>> getMyInvoices(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        List<Invoice> invoices = invoiceService.getMyInvoices(advocate);
        List<InvoiceResponseDTO> dtos = invoices.stream()
                .map(invoiceMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/create")
    @RequirePermission("INVOICE_CREATE")
    public ResponseEntity<InvoiceResponseDTO> createInvoice(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody InvoiceRequestDTO requestDTO) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Invoice invoice = invoiceMapper.toEntity(requestDTO);
        // Set the caseEntity stub so InvoiceService can resolve it
        advocate.com.advocate_app.entity.CaseEntity caseStub = new advocate.com.advocate_app.entity.CaseEntity();
        caseStub.setId(requestDTO.getCaseId());
        invoice.setCaseEntity(caseStub);
        Invoice created = invoiceService.createInvoice(email, invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceMapper.toResponseDTO(created));
    }

    @GetMapping("/summary")
    @RequirePermission("INVOICE_VIEW")
    public ResponseEntity<Map<String, Object>> getInvoiceSummary(
            @RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
        Map<String, Object> summary = invoiceService.getInvoiceSummary(advocate);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/pay/{id}")
    @RequirePermission("INVOICE_EDIT")
    public ResponseEntity<InvoiceResponseDTO> payInvoice(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Invoice paid = invoiceService.payInvoice(id, email);
        return ResponseEntity.ok(invoiceMapper.toResponseDTO(paid));
    }
}
