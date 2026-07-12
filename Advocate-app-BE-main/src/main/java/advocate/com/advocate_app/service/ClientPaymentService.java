package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientPaymentService {

    private static final Logger log = LoggerFactory.getLogger(ClientPaymentService.class);

    @Autowired
    private ClientPaymentRepository paymentRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private CaseFinancialService caseFinancialService;

    @Autowired
    private CaseTimelineService timelineService;

    @Autowired
    private AuditLogService auditLogService;

    public Page<ClientPayment> getPaymentsPaged(String email, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return paymentRepository.findByAdvocate(advocate, pageable);
    }

    // ✅ Create Payment
    public ClientPayment createPayment(String email, ClientPayment payment) {
        if (payment.getCaseEntity() == null || payment.getCaseEntity().getId() == null) {
            throw new RuntimeException("Case ID is required for client payment.");
        }

        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found."));

        CaseEntity caseEntity = caseRepository.findById(payment.getCaseEntity().getId())
                .orElseThrow(() -> new RuntimeException("Case not found."));

        if (!caseEntity.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to record payment for this case.");
        }

        payment.setCaseEntity(caseEntity);
        payment.setClient(caseEntity.getClient());
        payment.setAdvocate(advocate);

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(new Date());
        }

        ClientPayment saved = paymentRepository.save(payment);
        caseFinancialService.recalculateCaseFinancials(caseEntity.getId());

        // Record timeline
        try {
            String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String amount = saved.getAmount() != null ? String.format("₹%.0f", saved.getAmount()) : "";
            timelineService.recordEvent(
                    caseEntity.getId(), caseEntity.getClient() != null ? caseEntity.getClient().getId() : null,
                    advocate.getId(), CaseTimelineService.PAYMENT_RECEIVED,
                    "Payment Received",
                    amount + " received" + (saved.getPaymentMode() != null ? " via " + saved.getPaymentMode() : ""),
                    performedBy, "Payment", saved.getId()
            );
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String auditAmount = saved.getAmount() != null ? String.format("₹%.0f", saved.getAmount()) : "";
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.PAYMENT_RECEIVED, AuditLogService.MODULE_PAYMENTS,
                    "Payment Received",
                    auditAmount + " received" + (saved.getPaymentMode() != null ? " via " + saved.getPaymentMode() : ""),
                    "Payment", saved.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        return saved;
    }

    // ✅ Get Payments by Case
    public List<ClientPayment> getPaymentsByCase(String email, Long caseId) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found."));
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        if (!caseEntity.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized access to case payments");
        }
        return paymentRepository.findByCaseEntity(caseEntity);
    }

    // ✅ Today's Summary
    public Map<String, Object> getTodaySummary(String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found."));
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Date start = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<ClientPayment> payments = paymentRepository.findByAdvocateAndPaymentDateBetween(advocate, start, end);

        double totalAmount = payments.stream()
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();

        return Map.of(
                "date", today.toString(),
                "totalAmount", totalAmount,
                "totalCount", payments.size(),
                "payments", payments
        );
    }

    // ✅ Monthly Summary
    public Map<String, Object> getMonthlySummary(String email, int year, int month) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found."));
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);

        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<ClientPayment> payments = paymentRepository.findByAdvocateAndPaymentDateBetween(advocate, startDate, endDate);

        double totalAmount = payments.stream()
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();

        Map<String, Double> byMode = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> Optional.ofNullable(p.getPaymentMode()).orElse("Unknown"),
                        Collectors.summingDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                ));

        return Map.of(
                "year", year,
                "month", month,
                "totalAmount", totalAmount,
                "totalCount", payments.size(),
                "modeBreakdown", byMode,
                "payments", payments
        );
    }


}
