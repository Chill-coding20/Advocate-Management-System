package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.*;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired private CaseRepository caseRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private CaseEventRepository caseEventRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ClientPaymentRepository clientPaymentRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private ActivityRepository activityRepository;
    @Autowired private TaskRepository taskRepository;

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    public DashboardSummaryDTO getSummary(Advocate advocate) {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setTotalCases(caseRepository.countByAdvocateAndDeletedFalse(advocate));
        dto.setActiveCases(caseRepository.countByAdvocateAndStatusAndDeletedFalse(advocate, "Active"));
        dto.setClients(clientRepository.countByAdvocate(advocate));
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(30);
        dto.setUpcomingHearings(caseEventRepository.countUpcomingForAdvocate(advocate, now, end));
        dto.setPendingInvoices(invoiceRepository.countByAdvocateAndStatus(advocate, "UNPAID") +
                               invoiceRepository.countByAdvocateAndStatus(advocate, "OVERDUE"));
        return dto;
    }

    public DashboardSummaryDTO getSummary(Advocate advocate, LocalDate start, LocalDate end) {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        try { dto.setTotalCases(caseRepository.countByAdvocateAndDeletedFalseBetween(advocate, start, end)); }
        catch (Exception e) { log.error("Failed to count totalCases for advocate {}: {}", advocate.getEmail(), e.getMessage()); }
        try { dto.setActiveCases(caseRepository.countByAdvocateAndStatusAndDeletedFalseBetween(advocate, "Active", start, end)); }
        catch (Exception e) { log.error("Failed to count activeCases for advocate {}: {}", advocate.getEmail(), e.getMessage()); }
        try {
            long clientCount = (long) clientRepository.findAllActiveByAdvocate(advocate).size();
            log.info("CLIENT COUNT DEBUG: advocate={} start={} end={} method=findAllActiveByAdvocate(advocate).size() returned={}", advocate.getEmail(), start, end, clientCount);
            dto.setClients(clientCount);
        } catch (Exception e) {
            log.error("Failed to count clients for advocate {}: {}", advocate.getEmail(), e.getMessage());
        }
        try { dto.setUpcomingHearings(caseEventRepository.countUpcomingForAdvocate(advocate, start, end)); }
        catch (Exception e) { log.error("Failed to count upcomingHearings for advocate {}: {}", advocate.getEmail(), e.getMessage()); }
        try {
            long unpaid = invoiceRepository.countByAdvocateAndStatusBetween(advocate, "UNPAID", start, end);
            long overdue = invoiceRepository.countByAdvocateAndStatusBetween(advocate, "OVERDUE", start, end);
            dto.setPendingInvoices(unpaid + overdue);
        } catch (Exception e) { log.error("Failed to count pendingInvoices for advocate {}: {}", advocate.getEmail(), e.getMessage()); }
        return dto;
    }

    public CaseStatusDTO getCaseStatus(Advocate advocate) {
        long total = caseRepository.countByAdvocateAndDeletedFalse(advocate);
        List<Object[]> rows = caseRepository.countByStatusGrouped(advocate);
        return buildCaseStatus(total, rows);
    }

    public CaseStatusDTO getCaseStatus(Advocate advocate, LocalDate start, LocalDate end) {
        long total = caseRepository.countByAdvocateAndDeletedFalseBetween(advocate, start, end);
        List<Object[]> rows = caseRepository.countByStatusGroupedBetween(advocate, start, end);
        return buildCaseStatus(total, rows);
    }

    private CaseStatusDTO buildCaseStatus(long total, List<Object[]> rows) {
        long finalTotal = Math.max(total, 1);
        List<String> allStatuses = Arrays.asList("Active", "Pending", "Closed", "Dismissed");
        Map<String, Long> map = new LinkedHashMap<>();
        for (String s : allStatuses) map.put(s, 0L);
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        List<CaseStatusDTO.StatusItem> items = map.entrySet().stream()
                .map(e -> new CaseStatusDTO.StatusItem(e.getKey(), e.getValue(),
                        Math.round((e.getValue() * 100.0 / finalTotal) * 10.0) / 10.0))
                .collect(Collectors.toList());
        return new CaseStatusDTO(items);
    }

    public CourtStatsDTO getCourtStats(Advocate advocate) {
        return buildCourtStats(caseRepository.countByCourtAndStatusGrouped(advocate));
    }

    public CourtStatsDTO getCourtStats(Advocate advocate, LocalDate start, LocalDate end) {
        return buildCourtStats(caseRepository.countByCourtAndStatusGroupedBetween(advocate, start, end));
    }

    private CourtStatsDTO buildCourtStats(List<Object[]> rows) {
        log.debug("Building court stats from {} rows", rows.size());

        List<String> courts = Arrays.asList("District Court", "High Court", "Supreme Court");
        List<String> statuses = Arrays.asList("Active", "Pending", "Closed", "Dismissed");
        Map<String, Map<String, Long>> grid = new LinkedHashMap<>();
        for (String c : courts) {
            grid.put(c, new LinkedHashMap<>());
            for (String s : statuses) grid.get(c).put(s, 0L);
        }
        for (Object[] row : rows) {
            String rawCourt = (String) row[0];
            String normalized = normalizeCourt(rawCourt);
            String status = (String) row[1];
            Long count = (Long) row[2];
            log.debug("Court stat: raw={} normalized={} status={} count={}", rawCourt, normalized, status, count);
            if (normalized != null && grid.containsKey(normalized)) {
                grid.get(normalized).put(status, count);
            }
        }
        log.debug("Aggregation grid built");
        List<CourtStatsDTO.CourtItem> items = courts.stream()
                .map(c -> new CourtStatsDTO.CourtItem(c,
                        grid.get(c).get("Active"),
                        grid.get(c).get("Pending"),
                        grid.get(c).get("Closed"),
                        grid.get(c).get("Dismissed")))
                .collect(Collectors.toList());
        return new CourtStatsDTO(items);
    }

    public MonthlyCaseDTO getMonthlyCases(Advocate advocate) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Map<Integer, long[]> monthMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) monthMap.put(i, new long[]{0, 0, 0, 0});
        List<Object[]> rows = caseRepository.monthlyCaseStatus(advocate);
        for (Object[] row : rows) {
            int m = ((Number) row[0]).intValue();
            long active = row[1] != null ? ((Number) row[1]).longValue() : 0;
            long closed = row[2] != null ? ((Number) row[2]).longValue() : 0;
            long pending = row[3] != null ? ((Number) row[3]).longValue() : 0;
            long dismissed = row[4] != null ? ((Number) row[4]).longValue() : 0;
            monthMap.put(m, new long[]{active, closed, pending, dismissed});
        }
        int currentMonth = LocalDate.now().getMonthValue();
        List<MonthlyCaseDTO.MonthItem> items = new ArrayList<>();
        for (int i = 1; i <= currentMonth; i++) {
            long[] vals = monthMap.get(i);
            items.add(new MonthlyCaseDTO.MonthItem(months[i - 1], vals[0], vals[1], vals[2], vals[3]));
        }
        return new MonthlyCaseDTO(items);
    }

    public MonthlyCaseDTO getMonthlyCases(Advocate advocate, LocalDate start, LocalDate end) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Map<Integer, long[]> monthMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) monthMap.put(i, new long[]{0, 0, 0, 0});
        List<Object[]> rows = caseRepository.monthlyCaseStatusBetween(advocate, start, end);
        for (Object[] row : rows) {
            int m = ((Number) row[0]).intValue();
            long active = row[1] != null ? ((Number) row[1]).longValue() : 0;
            long closed = row[2] != null ? ((Number) row[2]).longValue() : 0;
            long pending = row[3] != null ? ((Number) row[3]).longValue() : 0;
            long dismissed = row[4] != null ? ((Number) row[4]).longValue() : 0;
            monthMap.put(m, new long[]{active, closed, pending, dismissed});
        }
        int startMonth = start.getMonthValue();
        int endMonth = end.getMonthValue();
        List<MonthlyCaseDTO.MonthItem> items = new ArrayList<>();
        int month = startMonth;
        while (true) {
            long[] vals = monthMap.get(month);
            items.add(new MonthlyCaseDTO.MonthItem(months[month - 1], vals[0], vals[1], vals[2], vals[3]));
            if (month == endMonth) break;
            month++;
            if (month > 12) month = 1;
        }
        return new MonthlyCaseDTO(items);
    }

    public IncomeExpenseDTO getIncomeExpense(Advocate advocate) {
        int year = LocalDate.now().getYear();
        return buildIncomeExpense(advocate, year, 1, year, LocalDate.now().getMonthValue());
    }

    public IncomeExpenseDTO getIncomeExpense(Advocate advocate, LocalDate start, LocalDate end) {
        int startYear = start.getYear();
        int startMonth = start.getMonthValue();
        int endYear = end.getYear();
        int endMonth = end.getMonthValue();
        return buildIncomeExpense(advocate, startYear, startMonth, endYear, endMonth);
    }

    private IncomeExpenseDTO buildIncomeExpense(Advocate advocate, int startYear, int startMonth, int endYear, int endMonth) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<IncomeExpenseDTO.MonthEntry> items = new ArrayList<>();
        int y = startYear;
        int m = startMonth;
        while (y < endYear || (y == endYear && m <= endMonth)) {
            LocalDate start = LocalDate.of(y, m, 1);
            LocalDate monthEnd = start.withDayOfMonth(start.lengthOfMonth());
            double income = Optional.ofNullable(
                    clientPaymentRepository.sumByAdvocateAndDateBetween(advocate, start, monthEnd)
            ).orElse(0.0);
            double expense = Optional.ofNullable(
                    expenseRepository.sumByAdvocateAndDateBetween(advocate, start, monthEnd)
            ).orElse(0.0);
            items.add(new IncomeExpenseDTO.MonthEntry(months[m - 1], income, expense));
            m++;
            if (m > 12) { m = 1; y++; }
        }
        return new IncomeExpenseDTO(items);
    }

    public DashboardDTO getFilteredDashboard(Advocate advocate, LocalDate start, LocalDate end) {
        DashboardDTO dto = new DashboardDTO();
        try {
            dto.setSummary(getSummary(advocate, start, end));
            log.info("FILTERED DASHBOARD SUMMARY: advocate={} start={} end={} clients={} totalCases={} activeCases={}",
                    advocate.getEmail(), start, end,
                    dto.getSummary().getClients(),
                    dto.getSummary().getTotalCases(),
                    dto.getSummary().getActiveCases());
        } catch (Exception e) {
            log.error("Dashboard summary failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setSummary(new DashboardSummaryDTO());
        }

        try {
            dto.setCaseStatus(getCaseStatus(advocate, start, end));
        } catch (Exception e) {
            log.error("Dashboard caseStatus failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setCaseStatus(new CaseStatusDTO(new ArrayList<>()));
        }

        try {
            dto.setCourtStats(getCourtStats(advocate, start, end));
        } catch (Exception e) {
            log.error("Dashboard courtStats failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setCourtStats(new CourtStatsDTO(new ArrayList<>()));
        }

        try {
            dto.setMonthlyCases(getMonthlyCases(advocate, start, end));
        } catch (Exception e) {
            log.error("Dashboard monthlyCases failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setMonthlyCases(new MonthlyCaseDTO(new ArrayList<>()));
        }

        try {
            dto.setIncomeExpense(getIncomeExpense(advocate, start, end));
        } catch (Exception e) {
            log.error("Dashboard incomeExpense failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setIncomeExpense(new IncomeExpenseDTO(new ArrayList<>()));
        }

        // Hearings in range
        try {
            List<CaseEventEntity> hearings = caseEventRepository.findUpcomingEvents(advocate, start, end).stream()
                    .filter(e -> "HEARING".equalsIgnoreCase(e.getEventType()))
                    .limit(5)
                    .collect(Collectors.toList());
            dto.setHearings(hearings);
        } catch (Exception e) {
            log.error("Dashboard hearings failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setHearings(new ArrayList<>());
        }

        // Invoice summary
        try {
            List<Invoice> allInvoices = invoiceRepository.findByAdvocate(advocate);
            List<Invoice> periodInvoices = allInvoices.stream()
                    .filter(i -> i.getInvoiceDate() != null
                            && !i.getInvoiceDate().isBefore(start)
                            && !i.getInvoiceDate().isAfter(end))
                    .collect(Collectors.toList());
            double paidTotal = periodInvoices.stream().filter(i -> "PAID".equalsIgnoreCase(i.getStatus()))
                    .mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0).sum();
            double unpaidTotal = periodInvoices.stream().filter(i -> "UNPAID".equalsIgnoreCase(i.getStatus()))
                    .mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0).sum();
            double overdueTotal = periodInvoices.stream().filter(i -> "OVERDUE".equalsIgnoreCase(i.getStatus()))
                    .mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0).sum();
            Map<String, Object> invSummary = Map.of("paid", paidTotal, "unpaid", unpaidTotal, "overdue", overdueTotal);
            dto.setInvoiceSummary(invSummary);

            // Recent invoices
            List<Invoice> recentInvoices = periodInvoices.stream()
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(5)
                    .collect(Collectors.toList());
            dto.setInvoices(recentInvoices);
        } catch (Exception e) {
            log.error("Dashboard invoices failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setInvoiceSummary(Map.of("paid", 0.0, "unpaid", 0.0, "overdue", 0.0));
            dto.setInvoices(new ArrayList<>());
        }

        // Activities and tasks
        try {
            List<Activity> activities = activityRepository.findByAdvocateOrderByTimestampDesc(advocate).stream()
                    .filter(a -> a.getTimestamp() != null
                            && !a.getTimestamp().toLocalDate().isBefore(start)
                            && !a.getTimestamp().toLocalDate().isAfter(end))
                    .limit(10).collect(Collectors.toList());
            dto.setActivities(activities);
        } catch (Exception e) {
            log.error("Dashboard activities failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setActivities(new ArrayList<>());
        }

        try {
            List<Task> tasks = taskRepository.findByAdvocateOrderByCompletedAscDeadlineAsc(advocate).stream()
                    .limit(5).collect(Collectors.toList());
            dto.setTasks(tasks);
        } catch (Exception e) {
            log.error("Dashboard tasks failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setTasks(new ArrayList<>());
        }

        // Recent clients (all active, sorted by newest first)
        try {
            List<Client> recentClients = clientRepository.findAllActiveByAdvocate(advocate).stream()
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(5)
                    .collect(Collectors.toList());
            dto.setRecentClients(recentClients);
        } catch (Exception e) {
            log.error("Dashboard recentClients failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setRecentClients(new ArrayList<>());
        }

        // Recent cases
        try {
            List<CaseEntity> recentCases = caseRepository.findByAdvocate(advocate).stream()
                    .filter(c -> !c.isDeleted()
                            && c.getCreatedAt() != null
                            && !c.getCreatedAt().isBefore(start)
                            && !c.getCreatedAt().isAfter(end))
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(5)
                    .collect(Collectors.toList());
            dto.setRecentCases(recentCases);
        } catch (Exception e) {
            log.error("Dashboard recentCases failed for advocate {}: {}", advocate.getEmail(), e.getMessage(), e);
            dto.setRecentCases(new ArrayList<>());
        }

        return dto;
    }

    private String normalizeCourt(String court) {
        if (court == null) return null;
        String c = court.trim().toLowerCase().replaceAll("\\s+", "");
        if ("district".equals(c) || "districtcourt".equals(c)) return "District Court";
        if ("high".equals(c) || "highcourt".equals(c)) return "High Court";
        if ("supreme".equals(c) || "supremecourt".equals(c)) return "Supreme Court";
        return court;
    }
}
