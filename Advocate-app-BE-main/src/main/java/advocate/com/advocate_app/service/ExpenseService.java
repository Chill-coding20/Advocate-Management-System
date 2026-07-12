package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.entity.Expense;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.CaseRepository;
import advocate.com.advocate_app.repository.ExpenseRepository;
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
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final AdvocateRepository advocateRepository;
    private final CaseRepository caseRepository;
    private final CaseFinancialService caseFinancialService;

    @Autowired
    private CaseTimelineService timelineService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository,
                          AdvocateRepository advocateRepository,
                          CaseRepository caseRepository,
                          CaseFinancialService caseFinancialService) {
        this.expenseRepository = expenseRepository;
        this.advocateRepository = advocateRepository;
        this.caseRepository = caseRepository;
        this.caseFinancialService = caseFinancialService;
    }

    public Page<Expense> getExpensesPaged(String email, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return expenseRepository.findByAdvocate(advocate, pageable);
    }

    // ✅ Create Expense
    public Expense createExpense(String advocateEmail, Expense expense) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        expense.setAdvocate(advocate);

        // Default payment date = today if null
        if (expense.getPaymentDate() == null) {
            expense.setPaymentDate(new Date());
        }

        if ("GENERAL".equalsIgnoreCase(expense.getExpenseType())) {
            expense.setCaseEntity(null);
            expense.setClient(null);
            return expenseRepository.save(expense);
        }

        if (expense.getCaseEntity() == null || expense.getCaseEntity().getId() == null) {
            throw new RuntimeException("Case must be selected for client case expenses.");
        }

        CaseEntity caseEntity = caseRepository.findById(expense.getCaseEntity().getId())
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (!caseEntity.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to create expense for this case");
        }

        expense.setCaseEntity(caseEntity);
        Client client = caseEntity.getClient();
        if (client != null) expense.setClient(client);

        Expense saved = expenseRepository.save(expense);
        caseFinancialService.recalculateCaseFinancials(caseEntity.getId());

        // Record timeline
        try {
            String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            timelineService.recordEvent(
                    caseEntity.getId(), caseEntity.getClient() != null ? caseEntity.getClient().getId() : null,
                    advocate.getId(), CaseTimelineService.EXPENSE_ADDED,
                    "Expense Added",
                    saved.getTitle() + " — ₹" + String.format("%.0f", saved.getAmount() != null ? saved.getAmount() : 0),
                    performedBy, "Expense", saved.getId()
            );
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.EXPENSE_CREATED, AuditLogService.MODULE_EXPENSES,
                    "Expense Created", saved.getTitle() + " — ₹" + String.format("%.0f", saved.getAmount() != null ? saved.getAmount() : 0),
                    "Expense", saved.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        return saved;
    }

    // ✅ Update Expense
    public Expense updateExpense(String advocateEmail, Long id, Expense updatedExpense) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!existing.getAdvocate().getId().equals(advocate.getId()))
            throw new RuntimeException("Unauthorized update attempt");

        existing.setTitle(updatedExpense.getTitle());
        existing.setAmount(updatedExpense.getAmount());
        existing.setCategory(updatedExpense.getCategory());
        existing.setDescription(updatedExpense.getDescription());
        existing.setPaymentMode(updatedExpense.getPaymentMode());
        existing.setPaymentStatus(updatedExpense.getPaymentStatus());
        existing.setReferenceNumber(updatedExpense.getReferenceNumber());
        existing.setPaymentDate(
                updatedExpense.getPaymentDate() != null ? updatedExpense.getPaymentDate() : new Date()
        );
        existing.setExpenseType(
                updatedExpense.getExpenseType() != null ? updatedExpense.getExpenseType() : "CLIENT_CASE"
        );

        if (updatedExpense.getCaseEntity() != null && updatedExpense.getCaseEntity().getId() != null) {
            CaseEntity caseEntity = caseRepository.findById(updatedExpense.getCaseEntity().getId())
                    .orElseThrow(() -> new RuntimeException("Case not found"));
            if (!caseEntity.getAdvocate().getId().equals(advocate.getId())) {
                throw new RuntimeException("Unauthorized: case does not belong to this advocate");
            }
            existing.setCaseEntity(caseEntity);
            existing.setClient(caseEntity.getClient());
            expenseRepository.save(existing);
            caseFinancialService.recalculateCaseFinancials(caseEntity.getId());
        } else {
            existing.setCaseEntity(null);
            existing.setClient(null);
            expenseRepository.save(existing);
        }

        // Record timeline
        try {
            CaseEntity caseForTimeline = existing.getCaseEntity();
            if (caseForTimeline != null) {
                String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
                timelineService.recordEvent(
                        caseForTimeline.getId(), caseForTimeline.getClient() != null ? caseForTimeline.getClient().getId() : null,
                        advocate.getId(), CaseTimelineService.EXPENSE_UPDATED,
                        "Expense Updated",
                        existing.getTitle() + " updated",
                        performedBy, "Expense", existing.getId()
                );
            }
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.EXPENSE_UPDATED, AuditLogService.MODULE_EXPENSES,
                    "Expense Updated", existing.getTitle() + " updated",
                    "Expense", existing.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        return existing;
    }

    // ✅ Get all Expenses for an Advocate
    public List<Expense> getMyExpenses(String advocateEmail) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        return expenseRepository.findByAdvocate(advocate);
    }

    // ✅ Get Expenses by Case
    public List<Expense> getExpensesByCase(String advocateEmail, Long caseId) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        if (!caseEntity.getAdvocate().getId().equals(advocate.getId()))
            throw new RuntimeException("Unauthorized access to this case's expenses");

        return expenseRepository.findByCaseEntity(caseEntity);
    }

    // ✅ Delete Expense
    public void deleteExpense(String advocateEmail, Long id) {
        Advocate advocate = advocateRepository.findByEmail(advocateEmail)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        Expense exp = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!exp.getAdvocate().getId().equals(advocate.getId()))
            throw new RuntimeException("Unauthorized delete attempt");

        CaseEntity caseEntity = exp.getCaseEntity();
        String expTitle = exp.getTitle();
        Long caseIdForTimeline = caseEntity != null ? caseEntity.getId() : null;
        Long clientIdForTimeline = caseEntity != null && caseEntity.getClient() != null ? caseEntity.getClient().getId() : null;

        expenseRepository.delete(exp);
        if (caseEntity != null) caseFinancialService.recalculateCaseFinancials(caseEntity.getId());

        // Record timeline
        if (caseIdForTimeline != null) {
            try {
                String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
                timelineService.recordEvent(
                        caseIdForTimeline, clientIdForTimeline, advocate.getId(),
                        CaseTimelineService.EXPENSE_DELETED,
                        "Expense Deleted",
                        expTitle + " removed",
                        performedBy, "Expense", id
                );
            } catch (Exception e) {
                log.warn("Could not record timeline event: {}", e.getMessage());
            }
        }

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.EXPENSE_DELETED, AuditLogService.MODULE_EXPENSES,
                    "Expense Deleted", expTitle + " removed",
                    "Expense", id, "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }
    }

    // ✅ Search
    public List<Expense> searchExpenses(String advocateEmail, String keyword) {
        List<Expense> all = getMyExpenses(advocateEmail);
        if (keyword == null || keyword.isBlank()) return all;
        String key = keyword.toLowerCase();
        return all.stream().filter(exp ->
                (exp.getTitle() != null && exp.getTitle().toLowerCase().contains(key)) ||
                        (exp.getCategory() != null && exp.getCategory().toLowerCase().contains(key)) ||
                        (exp.getPaymentStatus() != null && exp.getPaymentStatus().toLowerCase().contains(key)) ||
                        (exp.getReferenceNumber() != null && exp.getReferenceNumber().toLowerCase().contains(key))
        ).toList();
    }

    // ✅ Helper for date conversion
    private LocalDate safeConvert(Date date) {
        if (date == null) return null;
        try {
            return new java.sql.Date(date.getTime()).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    // ✅ Today's Expenses (Fixed timezone)
    public Map<String, Object> getTodayExpenses(String advocateEmail) {
        try {
            List<Expense> all = getMyExpenses(advocateEmail);
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            Date start = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<Expense> todayList = all.stream()
                    .filter(e -> e.getPaymentDate() != null &&
                            !e.getPaymentDate().before(start) &&
                            e.getPaymentDate().before(end))
                    .toList();

            double totalAmount = todayList.stream()
                    .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                    .sum();

            return Map.of(
                    "date", today.toString(),
                    "count", todayList.size(),
                    "totalAmount", totalAmount,
                    "expenses", todayList
            );
        } catch (Exception e) {
            log.error("Error fetching today's expenses for {}: {}", advocateEmail, e.getMessage(), e);
            return Map.of("error", "Unable to load today's expenses. Please try again.");
        }
    }

    // ✅ Monthly Report (Grouped by category)
    public Map<String, Object> getMonthlyReport(String advocateEmail, int year, int month) {
        try {
            List<Expense> all = getMyExpenses(advocateEmail);
            log.debug("Monthly report for year={}, month={}, totalExpenses={}", year, month, all.size());
            List<Expense> filtered = all.stream().filter(e -> {
                Date rawDate = e.getPaymentDate();
                LocalDate date = safeConvert(rawDate);
                log.debug("Expense id={} title={} rawDate={} parsed={}", e.getId(), e.getTitle(), rawDate, date);
                return date != null && date.getYear() == year && date.getMonthValue() == month;
            }).collect(Collectors.toList());
            log.debug("Monthly report filtered={}", filtered.size());

            double total = filtered.stream()
                    .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                    .sum();

            Map<String, Double> byCategory = filtered.stream()
                    .collect(Collectors.groupingBy(
                            e -> Optional.ofNullable(e.getCategory()).orElse("Uncategorized"),
                            Collectors.summingDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                    ));

            return Map.of(
                    "month", month,
                    "year", year,
                    "totalExpenses", total,
                    "categoryBreakdown", byCategory,
                    "totalCount", filtered.size(),
                    "expenses", filtered
            );
        } catch (Exception e) {
            log.error("Error fetching monthly report for {}: year={} month={}: {}", advocateEmail, year, month, e.getMessage(), e);
            return Map.of("error", "Unable to load monthly report. Please try again.");
        }
    }
}
