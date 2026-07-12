package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.CaseDetailReportDTO;
import advocate.com.advocate_app.dto.ClientDetailReportDTO;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.repository.*;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.AuditLogService;
import advocate.com.advocate_app.service.PdfGeneratorService;
import advocate.com.advocate_app.service.ReportService;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired private PdfGeneratorService pdfGeneratorService;
    @Autowired private ReportService reportService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private AdvocateRepository advocateRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private CaseRepository caseRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ClientPaymentRepository clientPaymentRepository;
    @Autowired private CaseEventRepository caseEventRepository;

    private Advocate getAdvocate(String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        return advocateRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Advocate not found"));
    }

    // ───── Existing endpoints ─────

    private void recordExportAudit(Advocate advocate, String reportName) {
        try {
            String userName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), userName,
                    AuditLogService.EXPORT_PDF, AuditLogService.MODULE_EXPORTS,
                    "PDF Export", reportName + " exported as PDF",
                    "Export", null, "SUCCESS"
            );
        } catch (Exception e) {
            // silently fail — audit is non-critical
        }
    }

    @GetMapping("/cases")
    public ResponseEntity<byte[]> getCaseReport(@RequestHeader("Authorization") String token) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        List<CaseEntity> cases = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted()).collect(Collectors.toList());
        recordExportAudit(advocate, "Case Report");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateCaseReport(out, cases, advocate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CASE_REPORT.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    @GetMapping("/clients")
    @RequirePermission("REPORT_VIEW")
    public ResponseEntity<byte[]> getClientReport(@RequestHeader("Authorization") String token) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        List<Client> clients = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && c.getClient() != null && !c.getClient().isDeleted())
                .map(CaseEntity::getClient).distinct().collect(Collectors.toList());
        recordExportAudit(advocate, "Client Report");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateClientReport(out, clients, advocate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CLIENT_REPORT.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    @GetMapping("/expenses")
    @RequirePermission("REPORT_VIEW")
    public ResponseEntity<byte[]> getExpenseReport(@RequestHeader("Authorization") String token) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        List<Expense> expenses = expenseRepository.findByAdvocate(advocate);
        recordExportAudit(advocate, "Expense Report");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateExpenseReport(out, expenses, advocate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=EXPENSE_REPORT.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    @GetMapping("/invoice/{id}")
    @RequirePermission("REPORT_VIEW")
    public ResponseEntity<byte[]> getInvoiceReport(@RequestHeader("Authorization") String token,
                                                   @PathVariable Long id) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        Invoice invoice = invoiceRepository.findById(id).orElse(null);
        if (invoice == null || !invoice.getAdvocate().getId().equals(advocate.getId()))
            return ResponseEntity.notFound().build();
        recordExportAudit(advocate, "Invoice " + invoice.getInvoiceNumber());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateInvoicePdf(out, invoice);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    @GetMapping("/receipt/{id}")
    @RequirePermission("REPORT_VIEW")
    public ResponseEntity<byte[]> getReceiptReport(@RequestHeader("Authorization") String token,
                                                   @PathVariable Long id) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        ClientPayment payment = clientPaymentRepository.findById(id).orElse(null);
        if (payment == null || !payment.getAdvocate().getId().equals(advocate.getId()))
            return ResponseEntity.notFound().build();
        recordExportAudit(advocate, "Receipt #" + payment.getId());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateReceiptPdf(out, payment);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=RECEIPT_" + payment.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    // ───── New endpoints ─────

    @GetMapping("/client/{id}/pdf")
    @RequirePermission("REPORT_EXPORT")
    public ResponseEntity<byte[]> getClientDetailPdf(@RequestHeader("Authorization") String token,
                                                     @PathVariable Long id) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        Client client = clientRepository.findById(id).orElse(null);
        if (client == null || !client.getAdvocate().getId().equals(advocate.getId()))
            return ResponseEntity.notFound().build();
        recordExportAudit(advocate, "Client Detail — " + client.getName());
        ClientDetailReportDTO dto = reportService.buildClientDetail(client, advocate);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateClientDetailReport(out, dto, advocate);
        String filename = "CLIENT_" + client.getName().toUpperCase().replaceAll("\\s+", "_") + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    @GetMapping("/case/{id}/pdf")
    @RequirePermission("REPORT_EXPORT")
    public ResponseEntity<byte[]> getCaseDetailPdf(@RequestHeader("Authorization") String token,
                                                   @PathVariable Long id) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        CaseEntity c = caseRepository.findById(id).orElse(null);
        if (c == null || !c.getAdvocate().getId().equals(advocate.getId()))
            return ResponseEntity.notFound().build();
        recordExportAudit(advocate, "Case Detail — " + c.getCaseNumber());
        CaseDetailReportDTO dto = reportService.buildCaseDetail(c, advocate);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateCaseDetailReport(out, dto, advocate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CASE_" + c.getCaseNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    @GetMapping("/monthly/pdf")
    @RequirePermission("REPORT_EXPORT")
    public ResponseEntity<byte[]> getMonthlyReport(@RequestHeader("Authorization") String token,
                                                   @RequestParam(required = false) Integer year,
                                                   @RequestParam(required = false) Integer month) throws DocumentException {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        Advocate advocate = getAdvocate(token);
        recordExportAudit(advocate, "Monthly Report — " + month + "/" + year);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<CaseEntity> allCases = caseRepository.findByAdvocate(advocate);
        long total = allCases.stream().filter(c -> !c.isDeleted()).count();
        long active = allCases.stream().filter(c -> !c.isDeleted() && "Active".equalsIgnoreCase(c.getStatus())).count();
        long closed = allCases.stream().filter(c -> !c.isDeleted() && "Closed".equalsIgnoreCase(c.getStatus())).count();
        long pending = allCases.stream().filter(c -> !c.isDeleted() && "Pending".equalsIgnoreCase(c.getStatus())).count();
        long dismissed = allCases.stream().filter(c -> !c.isDeleted() && "Dismissed".equalsIgnoreCase(c.getStatus())).count();

        long totalClients = clientRepository.countByAdvocate(advocate);
        long newClientsThisMonth = 0;
        double income = clientPaymentRepository.sumByAdvocateAndDateBetween(advocate, start, end);
        double expense = expenseRepository.sumByAdvocateAndDateBetween(advocate, start, end);
        double profit = income - expense;

        long hearings = caseEventRepository.countUpcomingForAdvocate(advocate, start, end);

        List<Invoice> invoicesThisMonth = invoiceRepository.findByAdvocate(advocate).stream()
                .filter(i -> i.getInvoiceDate() != null && !i.getInvoiceDate().isBefore(start) && !i.getInvoiceDate().isAfter(end))
                .collect(Collectors.toList());
        long invoicesGenerated = invoicesThisMonth.size();
        long paymentsReceived = clientPaymentRepository.findByAdvocate(advocate).stream()
                .filter(p -> p.getPaymentDate() != null) .count();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateMonthlyReport(out, advocate, year, month,
                totalClients, newClientsThisMonth, total, active, closed, pending, dismissed,
                income, expense, profit, hearings, invoicesGenerated, paymentsReceived);

        String monthName = start.format(java.time.format.DateTimeFormatter.ofPattern("MMMM_yyyy"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=MONTHLY_REPORT_" + monthName + ".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    @GetMapping("/expense/pdf")
    @RequirePermission("REPORT_EXPORT")
    public ResponseEntity<byte[]> getFilteredExpenseReport(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long caseId,
            @RequestParam(required = false) String category) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        recordExportAudit(advocate, "Filtered Expense Report");
        List<Expense> expenses = expenseRepository.findByAdvocate(advocate).stream()
                .filter(e -> startDate == null || startDate.isEmpty() ||
                        (e.getPaymentDate() != null && !new java.sql.Date(e.getPaymentDate().getTime()).toLocalDate().isBefore(LocalDate.parse(startDate))))
                .filter(e -> endDate == null || endDate.isEmpty() ||
                        (e.getPaymentDate() != null && !new java.sql.Date(e.getPaymentDate().getTime()).toLocalDate().isAfter(LocalDate.parse(endDate))))
                .filter(e -> caseId == null || (e.getCaseEntity() != null && e.getCaseEntity().getId().equals(caseId)))
                .filter(e -> category == null || category.isEmpty() || category.equalsIgnoreCase(e.getCategory()))
                .collect(Collectors.toList());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateExpenseReport(out, expenses, advocate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=EXPENSE_FILTERED_REPORT.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }

    @GetMapping("/dashboard/pdf")
    @RequirePermission("REPORT_EXPORT")
    public ResponseEntity<byte[]> getDashboardReport(@RequestHeader("Authorization") String token) throws DocumentException {
        Advocate advocate = getAdvocate(token);
        recordExportAudit(advocate, "Dashboard Report");
        Map<String, Object> data = reportService.buildDashboardReportData(advocate);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGeneratorService.generateDashboardReport(out, advocate, data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DASHBOARD_REPORT.pdf")
                .contentType(MediaType.APPLICATION_PDF).body(out.toByteArray());
    }
}
