package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.ReportsCenterDTO;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.ReportsCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports-center")
public class ReportsCenterController {

    private static final Logger log = LoggerFactory.getLogger(ReportsCenterController.class);

    private final ReportsCenterService reportsCenterService;

    public ReportsCenterController(ReportsCenterService reportsCenterService) {
        this.reportsCenterService = reportsCenterService;
    }

    @GetMapping
    @RequirePermission("REPORT_VIEW")
    public ResponseEntity<ReportsCenterDTO> getReports(
            @RequestParam(name = "filter", defaultValue = "this-month") String filter,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestHeader("Authorization") String token) {

        String email = JwtUtil.extractEmail(token.substring(7));
        log.info("ReportsCenter request - filter: {}, email: {}", filter, email);

        LocalDate start = startDate != null && !startDate.isEmpty() ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null && !endDate.isEmpty() ? LocalDate.parse(endDate) : null;

        ReportsCenterDTO dto = reportsCenterService.buildReport(email, filter, start, end);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/export/csv")
    @RequirePermission("REPORT_EXPORT")
    public ResponseEntity<String> exportCsv(
            @RequestParam(name = "section", defaultValue = "financial") String section,
            @RequestParam(name = "filter", defaultValue = "this-month") String filter,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestHeader("Authorization") String token) {

        String email = JwtUtil.extractEmail(token.substring(7));
        ReportsCenterDTO dto = reportsCenterService.buildReport(email, filter,
                startDate != null ? LocalDate.parse(startDate) : null,
                endDate != null ? LocalDate.parse(endDate) : null);

        String csv = buildCsv(section, dto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + section + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private String buildCsv(String section, ReportsCenterDTO dto) {
        StringBuilder sb = new StringBuilder();
        switch (section.toLowerCase()) {
            case "financial":
                sb.append("Metric,Current,Previous,Change\n");
                if (dto.getFinancial() != null) {
                    appendCsvRow(sb, "Revenue", dto.getFinancial().getRevenue());
                    appendCsvRow(sb, "Expenses", dto.getFinancial().getExpenses());
                    appendCsvRow(sb, "Net Income", dto.getFinancial().getNetIncome());
                    sb.append("Outstanding Payments,Total,Count,\n");
                    if (dto.getFinancial().getOutstandingPayments() != null) {
                        sb.append(",,,").append(dto.getFinancial().getOutstandingPayments().getTotal()).append(",").append(dto.getFinancial().getOutstandingPayments().getCount()).append("\n");
                    }
                    sb.append("\nMonth,Income,Expense\n");
                    if (dto.getFinancial().getCashFlow() != null) {
                        for (ReportsCenterDTO.CashFlowEntry e : dto.getFinancial().getCashFlow()) {
                            sb.append(e.getMonth()).append(",").append(e.getIncome()).append(",").append(e.getExpense()).append("\n");
                        }
                    }
                }
                break;
            case "cases":
                sb.append("Status,Count\n");
                if (dto.getCases() != null) {
                    sb.append("Active,").append(dto.getCases().getActive()).append("\n");
                    sb.append("Pending,").append(dto.getCases().getPending()).append("\n");
                    sb.append("Closed,").append(dto.getCases().getClosed()).append("\n");
                    sb.append("Dismissed,").append(dto.getCases().getDismissed()).append("\n");
                    sb.append("\nCourt,Count\n");
                    if (dto.getCases().getCourtDistribution() != null) {
                        for (ReportsCenterDTO.DistributionItem i : dto.getCases().getCourtDistribution()) {
                            sb.append(i.getName()).append(",").append(i.getCount()).append("\n");
                        }
                    }
                    sb.append("\nCase Type,Count\n");
                    if (dto.getCases().getTypeDistribution() != null) {
                        for (ReportsCenterDTO.DistributionItem i : dto.getCases().getTypeDistribution()) {
                            sb.append(i.getName()).append(",").append(i.getCount()).append("\n");
                        }
                    }
                }
                break;
            case "clients":
                sb.append("Metric,Value\n");
                if (dto.getClients() != null) {
                    if (dto.getClients().getNewClients() != null) {
                        sb.append("New Clients (Current),").append(dto.getClients().getNewClients().getCurrent()).append("\n");
                        sb.append("New Clients (Previous),").append(dto.getClients().getNewClients().getPrevious()).append("\n");
                        sb.append("Change (%),").append(dto.getClients().getNewClients().getChange()).append("\n");
                    }
                    if (dto.getClients().getPendingPayments() != null) {
                        sb.append("Pending Payments Total,").append(dto.getClients().getPendingPayments().getTotal()).append("\n");
                        sb.append("Pending Payments Count,").append(dto.getClients().getPendingPayments().getCount()).append("\n");
                    }
                    sb.append("\nMonth,New Clients\n");
                    if (dto.getClients().getGrowth() != null) {
                        for (ReportsCenterDTO.GrowthEntry g : dto.getClients().getGrowth()) {
                            sb.append(g.getMonth()).append(",").append(g.getCount()).append("\n");
                        }
                    }
                }
                break;
            case "hearings":
                sb.append("Category,Count\n");
                if (dto.getHearings() != null) {
                    sb.append("Today,").append(dto.getHearings().getToday()).append("\n");
                    sb.append("Upcoming,").append(dto.getHearings().getUpcoming()).append("\n");
                    sb.append("Missed,").append(dto.getHearings().getMissed()).append("\n");
                    sb.append("\nCourt,Count\n");
                    if (dto.getHearings().getCourtWise() != null) {
                        for (ReportsCenterDTO.DistributionItem i : dto.getHearings().getCourtWise()) {
                            sb.append(i.getName()).append(",").append(i.getCount()).append("\n");
                        }
                    }
                }
                break;
        }
        return sb.toString();
    }

    private void appendCsvRow(StringBuilder sb, String label, ReportsCenterDTO.Metric metric) {
        if (metric != null) {
            sb.append(label).append(",").append(metric.getCurrent()).append(",")
              .append(metric.getPrevious()).append(",").append(metric.getChange()).append("%\n");
        }
    }
}
