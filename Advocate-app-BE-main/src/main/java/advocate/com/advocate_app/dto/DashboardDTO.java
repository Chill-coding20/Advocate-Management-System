package advocate.com.advocate_app.dto;

import advocate.com.advocate_app.entity.Activity;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.CaseEventEntity;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.entity.Invoice;
import advocate.com.advocate_app.entity.Task;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private DashboardSummaryDTO summary;
    private CaseStatusDTO caseStatus;
    private CourtStatsDTO courtStats;
    private MonthlyCaseDTO monthlyCases;
    private IncomeExpenseDTO incomeExpense;
    private List<CaseEventEntity> hearings;
    private Map<String, Object> invoiceSummary;
    private List<Invoice> invoices;
    private List<Activity> activities;
    private List<Task> tasks;
    private List<Client> recentClients;
    private List<CaseEntity> recentCases;

    public DashboardSummaryDTO getSummary() { return summary; }
    public void setSummary(DashboardSummaryDTO summary) { this.summary = summary; }

    public CaseStatusDTO getCaseStatus() { return caseStatus; }
    public void setCaseStatus(CaseStatusDTO caseStatus) { this.caseStatus = caseStatus; }

    public CourtStatsDTO getCourtStats() { return courtStats; }
    public void setCourtStats(CourtStatsDTO courtStats) { this.courtStats = courtStats; }

    public MonthlyCaseDTO getMonthlyCases() { return monthlyCases; }
    public void setMonthlyCases(MonthlyCaseDTO monthlyCases) { this.monthlyCases = monthlyCases; }

    public IncomeExpenseDTO getIncomeExpense() { return incomeExpense; }
    public void setIncomeExpense(IncomeExpenseDTO incomeExpense) { this.incomeExpense = incomeExpense; }

    public List<CaseEventEntity> getHearings() { return hearings; }
    public void setHearings(List<CaseEventEntity> hearings) { this.hearings = hearings; }

    public Map<String, Object> getInvoiceSummary() { return invoiceSummary; }
    public void setInvoiceSummary(Map<String, Object> invoiceSummary) { this.invoiceSummary = invoiceSummary; }

    public List<Invoice> getInvoices() { return invoices; }
    public void setInvoices(List<Invoice> invoices) { this.invoices = invoices; }

    public List<Activity> getActivities() { return activities; }
    public void setActivities(List<Activity> activities) { this.activities = activities; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public List<Client> getRecentClients() { return recentClients; }
    public void setRecentClients(List<Client> recentClients) { this.recentClients = recentClients; }

    public List<CaseEntity> getRecentCases() { return recentCases; }
    public void setRecentCases(List<CaseEntity> recentCases) { this.recentCases = recentCases; }
}
