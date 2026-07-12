package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.ReportsCenterDTO;
import advocate.com.advocate_app.dto.ReportsCenterDTO.*;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsCenterService {

    private static final Logger log = LoggerFactory.getLogger(ReportsCenterService.class);
    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private final AdvocateRepository advocateRepository;
    private final CaseRepository caseRepository;
    private final ClientRepository clientRepository;
    private final CaseEventRepository caseEventRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientPaymentRepository clientPaymentRepository;
    private final ExpenseRepository expenseRepository;

    public ReportsCenterService(AdvocateRepository advocateRepository,
                                CaseRepository caseRepository,
                                ClientRepository clientRepository,
                                CaseEventRepository caseEventRepository,
                                InvoiceRepository invoiceRepository,
                                ClientPaymentRepository clientPaymentRepository,
                                ExpenseRepository expenseRepository) {
        this.advocateRepository = advocateRepository;
        this.caseRepository = caseRepository;
        this.clientRepository = clientRepository;
        this.caseEventRepository = caseEventRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientPaymentRepository = clientPaymentRepository;
        this.expenseRepository = expenseRepository;
    }

    public ReportsCenterDTO buildReport(String email, String filter, LocalDate startDate, LocalDate endDate) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found: " + email));

        LocalDate[] range = computeDateRange(filter, startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        LocalDate prevStart = range[2];
        LocalDate prevEnd = range[3];

        ReportsCenterDTO dto = new ReportsCenterDTO();

        try { dto.setFinancial(buildFinancial(advocate, start, end, prevStart, prevEnd)); }
        catch (Exception e) { log.error("Financial report failed: {}", e.getMessage()); }

        try { dto.setCases(buildCases(advocate, start, end)); }
        catch (Exception e) { log.error("Case report failed: {}", e.getMessage()); }

        try { dto.setClients(buildClients(advocate, start, end, prevStart, prevEnd)); }
        catch (Exception e) { log.error("Client report failed: {}", e.getMessage()); }

        try { dto.setHearings(buildHearings(advocate)); }
        catch (Exception e) { log.error("Hearing report failed: {}", e.getMessage()); }

        return dto;
    }

    // ── Date range computation ──
    private LocalDate[] computeDateRange(String filter, LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        LocalDate start, end, prevStart, prevEnd;

        switch (filter != null ? filter.toLowerCase() : "this-month") {
            case "today":
                start = now; end = now;
                prevStart = now.minusDays(1); prevEnd = now.minusDays(1);
                break;
            case "yesterday":
                start = now.minusDays(1); end = now.minusDays(1);
                prevStart = now.minusDays(2); prevEnd = now.minusDays(2);
                break;
            case "last7":
                start = now.minusDays(6); end = now;
                prevStart = now.minusDays(13); prevEnd = now.minusDays(7);
                break;
            case "last30":
                start = now.minusDays(29); end = now;
                prevStart = now.minusDays(59); prevEnd = now.minusDays(30);
                break;
            case "this-month":
                start = now.withDayOfMonth(1); end = now;
                prevStart = start.minusMonths(1); prevEnd = start.minusDays(1);
                break;
            case "last-month":
                start = now.minusMonths(1).withDayOfMonth(1);
                end = start.withDayOfMonth(start.lengthOfMonth());
                prevStart = start.minusMonths(1); prevEnd = start.minusDays(1);
                break;
            case "this-year":
                start = now.withDayOfYear(1); end = now;
                prevStart = start.minusYears(1); prevEnd = start.minusDays(1);
                break;
            case "custom":
                start = startDate != null ? startDate : now.minusDays(29);
                end = endDate != null ? endDate : now;
                long days = end.toEpochDay() - start.toEpochDay() + 1;
                prevStart = start.minusDays(days); prevEnd = start.minusDays(1);
                break;
            default:
                start = now.withDayOfMonth(1); end = now;
                prevStart = start.minusMonths(1); prevEnd = start.minusDays(1);
        }

        return new LocalDate[]{start, end, prevStart, prevEnd};
    }

    // ── Financial ──
    private FinancialReport buildFinancial(Advocate advocate, LocalDate start, LocalDate end,
                                           LocalDate prevStart, LocalDate prevEnd) {
        FinancialReport report = new FinancialReport();

        double revenue = getPaymentSum(advocate, start, end);
        double prevRevenue = getPaymentSum(advocate, prevStart, prevEnd);
        report.setRevenue(new Metric(revenue, prevRevenue));

        double expenses = getExpenseSum(advocate, start, end);
        double prevExpenses = getExpenseSum(advocate, prevStart, prevEnd);
        report.setExpenses(new Metric(expenses, prevExpenses));

        double outstanding = getOutstandingTotal(advocate);
        int outstandingCount = getOutstandingCount(advocate);
        report.setOutstandingPayments(new OutstandingMetric(outstanding, outstandingCount));

        double netIncome = revenue - expenses;
        double prevNetIncome = prevRevenue - prevExpenses;
        report.setNetIncome(new Metric(netIncome, prevNetIncome));

        report.setCashFlow(buildCashFlow(advocate, start, end));

        return report;
    }

    private double getPaymentSum(Advocate advocate, LocalDate start, LocalDate end) {
        return Optional.ofNullable(
                clientPaymentRepository.sumByAdvocateAndDateBetween(advocate, start.atStartOfDay().toLocalDate(),
                        end.atStartOfDay().toLocalDate())
        ).orElse(0.0);
    }

    private double getExpenseSum(Advocate advocate, LocalDate start, LocalDate end) {
        return Optional.ofNullable(
                expenseRepository.sumByAdvocateAndDateBetween(advocate, start, end)
        ).orElse(0.0);
    }

    private double getOutstandingTotal(Advocate advocate) {
        double unpaid = Optional.ofNullable(
                invoiceRepository.sumAmountByAdvocateAndStatus(advocate, "UNPAID")
        ).orElse(0.0);
        double overdue = Optional.ofNullable(
                invoiceRepository.sumAmountByAdvocateAndStatus(advocate, "OVERDUE")
        ).orElse(0.0);
        return unpaid + overdue;
    }

    private int getOutstandingCount(Advocate advocate) {
        return (int) (invoiceRepository.countByAdvocateAndStatus(advocate, "UNPAID")
                + invoiceRepository.countByAdvocateAndStatus(advocate, "OVERDUE"));
    }

    private List<CashFlowEntry> buildCashFlow(Advocate advocate, LocalDate start, LocalDate end) {
        List<CashFlowEntry> entries = new ArrayList<>();
        int startMonth = start.getMonthValue();
        int startYear = start.getYear();
        int endMonth = end.getMonthValue();
        int endYear = end.getYear();

        int y = startYear, m = startMonth;
        while (y < endYear || (y == endYear && m <= endMonth)) {
            LocalDate monthStart = LocalDate.of(y, m, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            double income = Optional.ofNullable(
                    clientPaymentRepository.sumByAdvocateAndDateBetween(advocate, monthStart, monthEnd)
            ).orElse(0.0);
            double expense = Optional.ofNullable(
                    expenseRepository.sumByAdvocateAndDateBetween(advocate, monthStart, monthEnd)
            ).orElse(0.0);
            entries.add(new CashFlowEntry(MONTHS[m - 1], income, expense));
            m++;
            if (m > 12) { m = 1; y++; }
        }
        return entries;
    }

    // ── Cases ──
    private CaseReport buildCases(Advocate advocate, LocalDate start, LocalDate end) {
        CaseReport report = new CaseReport();

        List<Object[]> statusRows = caseRepository.countByStatusGroupedBetween(advocate, start, end);
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusRows) statusMap.put((String) row[0], (Long) row[1]);

        report.setActive(statusMap.getOrDefault("Active", 0L).intValue());
        report.setPending(statusMap.getOrDefault("Pending", 0L).intValue());
        report.setClosed(statusMap.getOrDefault("Closed", 0L).intValue());
        report.setDismissed(statusMap.getOrDefault("Dismissed", 0L).intValue());

        List<Object[]> courtRows = caseRepository.countByCourtAndStatusGroupedBetween(advocate, start, end);
        Map<String, Long> courtMap = new LinkedHashMap<>();
        for (Object[] row : courtRows) {
            String court = normalizeCourt((String) row[0]);
            Long count = (Long) row[2];
            courtMap.merge(court, count, Long::sum);
        }
        report.setCourtDistribution(courtMap.entrySet().stream()
                .map(e -> new DistributionItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        List<Object[]> typeRows = caseRepository.countByTypeGroupedBetween(advocate, start, end);
        report.setTypeDistribution(typeRows.stream()
                .map(r -> new DistributionItem((String) r[0], (Long) r[1]))
                .collect(Collectors.toList()));

        return report;
    }

    private String normalizeCourt(String court) {
        if (court == null) return "Other";
        String c = court.trim().toLowerCase().replaceAll("\\s+", "");
        if (c.contains("district")) return "District Court";
        if (c.contains("high")) return "High Court";
        if (c.contains("supreme")) return "Supreme Court";
        return court;
    }

    // ── Clients ──
    private ClientReport buildClients(Advocate advocate, LocalDate start, LocalDate end,
                                      LocalDate prevStart, LocalDate prevEnd) {
        ClientReport report = new ClientReport();

        long newClientCount = clientRepository.findAllActiveByAdvocateAndCreatedAtBetween(advocate, start, end).size();
        long prevNewClientCount = clientRepository.findAllActiveByAdvocateAndCreatedAtBetween(advocate, prevStart, prevEnd).size();
        report.setNewClients(new Metric(newClientCount, prevNewClientCount));

        List<GrowthEntry> growth = new ArrayList<>();
        int startMonth = start.getMonthValue();
        int startYear = start.getYear();
        int endMonth = end.getMonthValue();
        int endYear = end.getYear();
        int y = startYear, m = startMonth;
        while (y < endYear || (y == endYear && m <= endMonth)) {
            LocalDate ms = LocalDate.of(y, m, 1);
            LocalDate me = ms.withDayOfMonth(ms.lengthOfMonth());
            long count = clientRepository.findAllActiveByAdvocateAndCreatedAtBetween(advocate, ms, me).size();
            growth.add(new GrowthEntry(MONTHS[m - 1], count));
            m++;
            if (m > 12) { m = 1; y++; }
        }
        report.setGrowth(growth);

        double pendingTotal = Optional.ofNullable(
                invoiceRepository.sumAmountByAdvocateAndStatus(advocate, "UNPAID")
        ).orElse(0.0) + Optional.ofNullable(
                invoiceRepository.sumAmountByAdvocateAndStatus(advocate, "OVERDUE")
        ).orElse(0.0);
        int pendingCount = getOutstandingCount(advocate);
        report.setPendingPayments(new OutstandingMetric(pendingTotal, pendingCount));

        return report;
    }

    // ── Hearings ──
    private HearingReport buildHearings(Advocate advocate) {
        HearingReport report = new HearingReport();
        LocalDate now = LocalDate.now();

        report.setToday(caseEventRepository.findByAdvocateAndDate(advocate, now).size());

        LocalDate weekEnd = now.plusDays(7);
        report.setUpcoming(caseEventRepository.findUpcomingEvents(advocate, now, weekEnd).size());

        LocalDate pastWeek = now.minusDays(7);
        long missed = caseEventRepository.findByAdvocateAndDateBetween(advocate, pastWeek, now).stream()
                .filter(e -> now.isAfter(e.getDate()))
                .count();
        report.setMissed((int) missed);

        List<Object[]> courtCounts = caseRepository.countByCourtAndStatusGrouped(advocate);
        Map<String, Long> courtMap = new LinkedHashMap<>();
        for (Object[] row : courtCounts) {
            String court = normalizeCourt((String) row[0]);
            Long count = (Long) row[2];
            courtMap.merge(court, count, Long::sum);
        }
        report.setCourtWise(courtMap.entrySet().stream()
                .map(e -> new DistributionItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        return report;
    }
}
