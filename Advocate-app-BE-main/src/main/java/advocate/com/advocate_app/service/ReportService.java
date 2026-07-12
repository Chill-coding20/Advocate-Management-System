package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.CaseDetailReportDTO;
import advocate.com.advocate_app.dto.ClientDetailReportDTO;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired private AdvocateRepository advocateRepository;
    @Autowired private CaseRepository caseRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ClientPaymentRepository clientPaymentRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private CaseEventRepository caseEventRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private PdfGeneratorService pdfGeneratorService;

    public Advocate getAdvocate(String email) {
        return advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
    }

    public ClientDetailReportDTO buildClientDetail(Client client, Advocate advocate) {
        ClientDetailReportDTO dto = new ClientDetailReportDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setPhone(client.getPhone());
        dto.setEmail(client.getEmail());
        dto.setAddress(client.getAddress());
        dto.setRegistrationDate(client.getId() != null ? "ID: " + client.getId() : "N/A");

        List<CaseEntity> clientCases = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && c.getClient() != null && c.getClient().getId().equals(client.getId()))
                .collect(Collectors.toList());

        dto.setTotalCases(clientCases.size());
        dto.setActiveCases(clientCases.stream().filter(c -> "Active".equalsIgnoreCase(c.getStatus())).count());
        dto.setClosedCases(clientCases.stream().filter(c -> "Closed".equalsIgnoreCase(c.getStatus())).count());
        dto.setPendingCases(clientCases.stream().filter(c -> "Pending".equalsIgnoreCase(c.getStatus())).count());

        List<String> docNames = documentRepository.findByAdvocateAndClient(advocate, client).stream()
                .map(Document::getDocumentName)
                .collect(Collectors.toList());
        dto.setDocuments(docNames);

        List<ClientPayment> payments = clientPaymentRepository.findByClientAndAdvocate(client, advocate);
        List<ClientDetailReportDTO.PaymentEntry> paymentEntries = payments.stream()
                .map(p -> {
                    ClientDetailReportDTO.PaymentEntry pe = new ClientDetailReportDTO.PaymentEntry();
                    pe.setDate(p.getPaymentDate() != null ? p.getPaymentDate().toString() : "N/A");
                    pe.setAmount(p.getAmount() != null ? p.getAmount() : 0);
                    pe.setMode(p.getPaymentMode());
                    pe.setReference(p.getReferenceNumber());
                    return pe;
                })
                .collect(Collectors.toList());
        dto.setRecentPayments(paymentEntries);

        return dto;
    }

    public CaseDetailReportDTO buildCaseDetail(CaseEntity c, Advocate advocate) {
        CaseDetailReportDTO dto = new CaseDetailReportDTO();
        dto.setId(c.getId());
        dto.setCaseNumber(c.getCaseNumber());
        dto.setCaseTitle(c.getCaseTitle());
        dto.setCaseType(c.getCaseType());
        dto.setCourtLevel(c.getCourtLevel());
        dto.setClientName(c.getClient() != null ? c.getClient().getName() : "N/A");
        dto.setStatus(c.getStatus());
        dto.setFiledDate(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "N/A");
        dto.setDescription(c.getDescription());
        dto.setAdvocateName(advocate.getFullName());
        dto.setNextHearing("N/A");

        // Next hearing from events
        List<CaseEventEntity> events = caseEventRepository.findByCaseEntityAndAdvocate(c, advocate);
        if (!events.isEmpty()) {
            CaseEventEntity next = events.stream()
                    .filter(e -> e.getDate() != null && !e.getDate().isBefore(LocalDate.now()))
                    .min(Comparator.comparing(CaseEventEntity::getDate))
                    .orElse(null);
            if (next != null) {
                dto.setNextHearing(next.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) +
                        (next.getTime() != null ? " " + next.getTime().toString() : ""));
            }
        }

        // Invoices
        List<Invoice> invoices = invoiceRepository.findByCaseEntityAndAdvocate(c, advocate);
        List<CaseDetailReportDTO.InvoiceEntry> invoiceEntries = invoices.stream().map(inv -> {
            CaseDetailReportDTO.InvoiceEntry ie = new CaseDetailReportDTO.InvoiceEntry();
            ie.setNumber(inv.getInvoiceNumber());
            ie.setAmount(inv.getAmount() != null ? inv.getAmount() : 0);
            ie.setStatus(inv.getStatus());
            return ie;
        }).collect(Collectors.toList());
        dto.setInvoices(invoiceEntries);

        // Expenses
        List<Expense> expenses = expenseRepository.findByCaseEntityAndAdvocate(c, advocate);
        double totalExp = expenses.stream().mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0).sum();
        dto.setTotalExpenses(totalExp);

        // Payments
        List<ClientPayment> payments = clientPaymentRepository.findByCaseEntityAndAdvocate(c, advocate);
        double totalPay = payments.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0).sum();
        dto.setTotalPayments(totalPay);

        // Documents
        List<String> docNames = documentRepository.findByAdvocateAndCaseEntity(advocate, c).stream()
                .map(Document::getDocumentName)
                .collect(Collectors.toList());
        dto.setDocuments(docNames);

        // Timeline
        List<CaseDetailReportDTO.TimelineEntry> timeline = events.stream().map(e -> {
            CaseDetailReportDTO.TimelineEntry te = new CaseDetailReportDTO.TimelineEntry();
            te.setDate(e.getDate() != null ? e.getDate().toString() : "");
            te.setEvent(e.getTitle() + (e.getDescription() != null ? " — " + e.getDescription() : ""));
            return te;
        }).collect(Collectors.toList());
        dto.setTimeline(timeline);

        return dto;
    }

    public Map<String, Object> buildDashboardReportData(Advocate advocate) {
        Map<String, Object> data = new LinkedHashMap<>();

        Map<String, Object> summary = new LinkedHashMap<>();
        List<CaseEntity> cases = caseRepository.findByAdvocate(advocate);
        long total = cases.stream().filter(c -> !c.isDeleted()).count();
        long active = cases.stream().filter(c -> !c.isDeleted() && "Active".equalsIgnoreCase(c.getStatus())).count();
        summary.put("totalCases", total);
        summary.put("activeCases", active);
        summary.put("totalClients", clientRepository.countByAdvocate(advocate));
        summary.put("upcomingHearings", caseEventRepository.countUpcomingForAdvocate(advocate, LocalDate.now(), LocalDate.now().plusMonths(1)));
        summary.put("pendingInvoices", invoiceRepository.findByAdvocate(advocate).stream()
                .filter(i -> "UNPAID".equalsIgnoreCase(i.getStatus())).count());
        data.put("summary", summary);

        Map<String, Object> financial = new LinkedHashMap<>();
        double income = clientPaymentRepository.sumByAdvocateAndDateBetween(advocate, LocalDate.now().minusMonths(1), LocalDate.now());
        double expensesAmt = expenseRepository.sumByAdvocateAndDateBetween(advocate, LocalDate.now().minusMonths(1), LocalDate.now());
        financial.put("income", income);
        financial.put("expenses", expensesAmt);
        data.put("financial", financial);

        Map<String, Long> caseStatus = new LinkedHashMap<>();
        for (CaseEntity c : cases) {
            if (!c.isDeleted()) {
                caseStatus.merge(c.getStatus() != null ? c.getStatus() : "Unknown", 1L, Long::sum);
            }
        }
        data.put("caseStatus", caseStatus);
        data.put("totalDocuments", documentRepository.countByAdvocate(advocate));

        return data;
    }
}
