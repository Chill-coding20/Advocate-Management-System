package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.*;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private CaseEventRepository caseEventRepository;

    @Autowired
    private ClientPaymentRepository clientPaymentRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Advocate getAdvocateFromToken(String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        return advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
    }

    // --- Unified Dashboard API with time filtering ---

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "month") String view,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        try {
            Advocate advocate = getAdvocateFromToken(token);
            int currentYear = year != null ? year : LocalDate.now().getYear();
            LocalDate start, end;

            switch (view.toLowerCase()) {
                case "day":
                    LocalDate day = date != null ? LocalDate.parse(date) : LocalDate.now();
                    start = day;
                    end = day.plusDays(1);
                    break;
                case "week":
                    int weekNum = week != null ? week : LocalDate.now().get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    start = LocalDate.ofYearDay(currentYear, 1)
                            .with(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNum)
                            .with(DayOfWeek.MONDAY);
                    end = start.plusDays(7);
                    break;
                case "month":
                    int m = month != null ? month : LocalDate.now().getMonthValue();
                    start = LocalDate.of(currentYear, m, 1);
                    end = start.withDayOfMonth(start.lengthOfMonth());
                    break;
                case "year":
                    start = LocalDate.of(currentYear, 1, 1);
                    end = LocalDate.of(currentYear, 12, 31);
                    break;
                default:
                    start = LocalDate.of(currentYear, 1, 1);
                    end = LocalDate.now();
            }

            log.info("Dashboard request: advocate={} view={} start={} end={} monthParam={} yearParam={}",
                    advocate.getEmail(), view, start, end, month, year);

            DashboardDTO dto = dashboardService.getFilteredDashboard(advocate, start, end);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            log.error("Dashboard endpoint failed: {}", e.getMessage(), e);
            DashboardDTO fallback = new DashboardDTO();
            fallback.setSummary(new DashboardSummaryDTO());
            fallback.setCaseStatus(new CaseStatusDTO(new ArrayList<>()));
            fallback.setCourtStats(new CourtStatsDTO(new ArrayList<>()));
            fallback.setMonthlyCases(new MonthlyCaseDTO(new ArrayList<>()));
            fallback.setIncomeExpense(new IncomeExpenseDTO(new ArrayList<>()));
            fallback.setHearings(new ArrayList<>());
            fallback.setInvoiceSummary(Map.of("paid", 0.0, "unpaid", 0.0, "overdue", 0.0));
            fallback.setInvoices(new ArrayList<>());
            fallback.setActivities(new ArrayList<>());
            fallback.setTasks(new ArrayList<>());
            fallback.setRecentClients(new ArrayList<>());
            fallback.setRecentCases(new ArrayList<>());
            return ResponseEntity.ok(fallback);
        }
    }

    // --- Consolidated Dashboard APIs ---

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(dashboardService.getSummary(getAdvocateFromToken(token)));
    }

    @GetMapping("/status")
    public ResponseEntity<CaseStatusDTO> getCaseStatus(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(dashboardService.getCaseStatus(getAdvocateFromToken(token)));
    }

    @GetMapping("/courts")
    public ResponseEntity<CourtStatsDTO> getCourtStats(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(dashboardService.getCourtStats(getAdvocateFromToken(token)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyCaseDTO> getMonthlyCases(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(dashboardService.getMonthlyCases(getAdvocateFromToken(token)));
    }

    @GetMapping("/income-expense")
    public ResponseEntity<IncomeExpenseDTO> getIncomeExpense(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(dashboardService.getIncomeExpense(getAdvocateFromToken(token)));
    }

    // --- Statistics Endpoints ---

    @GetMapping("/stats/cases-total")
    public ResponseEntity<?> getTotalCases(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        long count = caseRepository.findByAdvocate(advocate).stream().filter(c -> !c.isDeleted()).count();
        return ResponseEntity.ok(Map.of("value", count));
    }

    @GetMapping("/stats/cases-active")
    public ResponseEntity<?> getActiveCases(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        long count = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && "Active".equalsIgnoreCase(c.getStatus()))
                .count();
        return ResponseEntity.ok(Map.of("value", count));
    }

    @GetMapping("/stats/cases-pending")
    public ResponseEntity<?> getPendingCases(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        long count = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && "Pending".equalsIgnoreCase(c.getStatus()))
                .count();
        return ResponseEntity.ok(Map.of("value", count));
    }

    @GetMapping("/stats/cases-closed")
    public ResponseEntity<?> getClosedCases(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        long count = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && "Closed".equalsIgnoreCase(c.getStatus()))
                .count();
        return ResponseEntity.ok(Map.of("value", count));
    }

    @GetMapping("/stats/hearings-today")
    public ResponseEntity<?> getHearingsToday(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        long count = caseEventRepository.findByAdvocateAndDate(advocate, LocalDate.now()).stream()
                .filter(e -> "HEARING".equalsIgnoreCase(e.getEventType()))
                .count();
        return ResponseEntity.ok(Map.of("value", count));
    }

    @GetMapping("/stats/hearings-upcoming")
    public ResponseEntity<?> getHearingsUpcoming(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        // Next 30 days
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        long count = caseEventRepository.findUpcomingEvents(advocate, start, end).stream()
                .filter(e -> "HEARING".equalsIgnoreCase(e.getEventType()))
                .count();
        return ResponseEntity.ok(Map.of("value", count));
    }

    @GetMapping("/stats/clients-total")
    public ResponseEntity<?> getTotalClients(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        long count = clientRepository.findAllActiveByAdvocate(advocate).size();
        return ResponseEntity.ok(Map.of("value", count));
    }

    @GetMapping("/stats/clients-active")
    public ResponseEntity<?> getActiveClients(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        long count = clientRepository.findAllActiveByAdvocate(advocate).size();
        return ResponseEntity.ok(Map.of("value", count));
    }

    @GetMapping("/stats/monthly-income")
    public ResponseEntity<?> getMonthlyIncome(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(start.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        double total = clientPaymentRepository.findByAdvocate(advocate).stream()
                .filter(p -> p.getPaymentDate() != null && p.getPaymentDate().after(startDate) && p.getPaymentDate().before(endDate))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();
        return ResponseEntity.ok(Map.of("value", total));
    }

    @GetMapping("/stats/monthly-expenses")
    public ResponseEntity<?> getMonthlyExpenses(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(start.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        double total = expenseRepository.findByAdvocate(advocate).stream()
                .filter(e -> e.getPaymentDate() != null && e.getPaymentDate().after(startDate) && e.getPaymentDate().before(endDate))
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();
        return ResponseEntity.ok(Map.of("value", total));
    }

    @GetMapping("/stats/pending-payments")
    public ResponseEntity<?> getPendingPayments(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        double total = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted())
                .mapToDouble(c -> c.getPendingFromClient() != null ? c.getPendingFromClient() : 0.0)
                .sum();
        return ResponseEntity.ok(Map.of("value", total));
    }

    @GetMapping("/stats/overdue-invoices")
    public ResponseEntity<?> getOverdueInvoices(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        Double total = invoiceRepository.sumAmountByAdvocateAndStatus(advocate, "OVERDUE");
        return ResponseEntity.ok(Map.of("value", total != null ? total : 0.0));
    }

    // --- Chart Endpoints ---

    @GetMapping("/charts/case-status")
    public ResponseEntity<?> getCaseStatusChart(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        List<CaseEntity> cases = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted())
                .collect(Collectors.toList());

        Map<String, Long> counts = cases.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStatus() != null ? c.getStatus() : "Unknown",
                        Collectors.counting()
                ));

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/charts/case-category")
    public ResponseEntity<?> getCaseCategoryChart(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        List<CaseEntity> cases = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted())
                .collect(Collectors.toList());

        Map<String, Long> counts = cases.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCaseType() != null && !c.getCaseType().isBlank() ? c.getCaseType() : "General",
                        Collectors.counting()
                ));

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/charts/income-vs-expense")
    public ResponseEntity<?> getIncomeVsExpenseChart(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        
        // Compile summaries for the last 5 months
        List<Map<String, Object>> data = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 4; i >= 0; i--) {
            LocalDate targetMonth = now.minusMonths(i);
            String monthName = targetMonth.getMonth().toString().substring(0, 3); // JAN, FEB...
            
            LocalDate start = targetMonth.withDayOfMonth(1);
            Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(start.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            double income = clientPaymentRepository.findByAdvocate(advocate).stream()
                    .filter(p -> p.getPaymentDate() != null && p.getPaymentDate().after(startDate) && p.getPaymentDate().before(endDate))
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                    .sum();

            double expense = expenseRepository.findByAdvocate(advocate).stream()
                    .filter(e -> e.getPaymentDate() != null && e.getPaymentDate().after(startDate) && e.getPaymentDate().before(endDate))
                    .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                    .sum();

            data.add(Map.of(
                    "month", monthName,
                    "income", income,
                    "expense", expense
            ));
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/charts/client-growth")
    public ResponseEntity<?> getClientGrowthChart(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        List<CaseEntity> cases = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && c.getClient() != null)
                .collect(Collectors.toList());

        List<Map<String, Object>> data = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 4; i >= 0; i--) {
            LocalDate targetMonth = now.minusMonths(i);
            String monthName = targetMonth.getMonth().toString().substring(0, 3);
            
            // To provide real data without a created_at field, we approximate client growth 
            // by counting the distinct clients linked to cases. 
            // Since there's no historical timestamp, we return the total for current month.
            long count = 0;
            if (i == 0) {
                count = cases.stream()
                        .map(c -> c.getClient().getId())
                        .distinct()
                        .count();
            }
            
            data.add(Map.of(
                    "month", monthName,
                    "count", count
            ));
        }
        return ResponseEntity.ok(data);
    }

    // --- List / Widget Endpoints ---

    @GetMapping("/recent-cases")
    public ResponseEntity<?> getRecentCases(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        List<CaseEntity> cases = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted())
                .sorted((c1, c2) -> c2.getId().compareTo(c1.getId()))
                .limit(5)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/recent-clients")
    public ResponseEntity<?> getRecentClients(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        List<Client> clients = clientRepository.findAllActiveByAdvocate(advocate).stream()
                .sorted((c1, c2) -> c2.getId().compareTo(c1.getId()))
                .limit(4)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/hearings-today")
    public ResponseEntity<?> getHearingsTodayList(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        List<CaseEventEntity> hearings = caseEventRepository.findByAdvocateAndDate(advocate, LocalDate.now()).stream()
                .filter(e -> "HEARING".equalsIgnoreCase(e.getEventType()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(hearings);
    }

    @GetMapping("/hearings/upcoming")
    public ResponseEntity<?> getUpcomingHearings(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        List<CaseEventEntity> hearings = caseEventRepository.findUpcomingEvents(advocate, start, end).stream()
                .filter(e -> "HEARING".equalsIgnoreCase(e.getEventType()))
                .limit(3)
                .collect(Collectors.toList());
        return ResponseEntity.ok(hearings);
    }

    @GetMapping("/activities")
    public ResponseEntity<?> getActivities(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        List<Activity> activities = activityRepository.findByAdvocateOrderByTimestampDesc(advocate).stream()
                .limit(10)
                .collect(Collectors.toList());
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(@RequestHeader("Authorization") String token) {
        Advocate advocate = getAdvocateFromToken(token);
        List<Task> tasks = taskRepository.findByAdvocateOrderByCompletedAscDeadlineAsc(advocate).stream()
                .limit(5)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    // --- Global Search Endpoints ---

    @GetMapping("/global-search")
    public ResponseEntity<?> globalSearch(@RequestHeader("Authorization") String token,
                                          @RequestParam("keyword") String keyword) {
        Advocate advocate = getAdvocateFromToken(token);
        
        List<CaseEntity> matchedCases = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && (
                        c.getCaseNumber().toLowerCase().contains(keyword.toLowerCase()) ||
                        c.getCaseTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        c.getCaseType().toLowerCase().contains(keyword.toLowerCase())
                )).limit(5).collect(Collectors.toList());

        List<Client> matchedClients = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && c.getClient() != null && !c.getClient().isDeleted() && (
                        c.getClient().getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        c.getClient().getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                        c.getClient().getPhone().toLowerCase().contains(keyword.toLowerCase())
                )).map(CaseEntity::getClient).distinct().limit(5).collect(Collectors.toList());

        List<Expense> matchedExpenses = expenseRepository.findByAdvocate(advocate).stream()
                .filter(e -> e.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        e.getCategory().toLowerCase().contains(keyword.toLowerCase()))
                .limit(5).collect(Collectors.toList());

        List<Invoice> matchedInvoices = invoiceRepository.findByAdvocate(advocate).stream()
                .filter(i -> i.getInvoiceNumber().toLowerCase().contains(keyword.toLowerCase()) ||
                        i.getClient().getName().toLowerCase().contains(keyword.toLowerCase()))
                .limit(5).collect(Collectors.toList());

        List<Document> matchedDocuments = documentRepository.findByAdvocate(advocate).stream()
                .filter(d -> d.getDocumentName().toLowerCase().contains(keyword.toLowerCase()))
                .limit(5).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "cases", matchedCases,
                "clients", matchedClients,
                "expenses", matchedExpenses,
                "invoices", matchedInvoices,
                "documents", matchedDocuments
        ));
    }

}
