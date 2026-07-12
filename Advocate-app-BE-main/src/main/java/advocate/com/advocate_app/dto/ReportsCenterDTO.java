package advocate.com.advocate_app.dto;

import java.util.List;
import java.util.Map;

public class ReportsCenterDTO {

    private FinancialReport financial;
    private CaseReport cases;
    private ClientReport clients;
    private HearingReport hearings;

    public FinancialReport getFinancial() { return financial; }
    public void setFinancial(FinancialReport financial) { this.financial = financial; }

    public CaseReport getCases() { return cases; }
    public void setCases(CaseReport cases) { this.cases = cases; }

    public ClientReport getClients() { return clients; }
    public void setClients(ClientReport clients) { this.clients = clients; }

    public HearingReport getHearings() { return hearings; }
    public void setHearings(HearingReport hearings) { this.hearings = hearings; }

    // ── Financial ──
    public static class FinancialReport {
        private Metric revenue;
        private Metric expenses;
        private OutstandingMetric outstandingPayments;
        private Metric netIncome;
        private List<CashFlowEntry> cashFlow;

        public Metric getRevenue() { return revenue; }
        public void setRevenue(Metric revenue) { this.revenue = revenue; }

        public Metric getExpenses() { return expenses; }
        public void setExpenses(Metric expenses) { this.expenses = expenses; }

        public OutstandingMetric getOutstandingPayments() { return outstandingPayments; }
        public void setOutstandingPayments(OutstandingMetric outstandingPayments) { this.outstandingPayments = outstandingPayments; }

        public Metric getNetIncome() { return netIncome; }
        public void setNetIncome(Metric netIncome) { this.netIncome = netIncome; }

        public List<CashFlowEntry> getCashFlow() { return cashFlow; }
        public void setCashFlow(List<CashFlowEntry> cashFlow) { this.cashFlow = cashFlow; }
    }

    public static class Metric {
        private double current;
        private double previous;
        private double change;

        public Metric() {}
        public Metric(double current, double previous) {
            this.current = current;
            this.previous = previous;
            this.change = previous != 0 ? Math.round(((current - previous) / previous) * 1000.0) / 10.0 : 0;
        }

        public double getCurrent() { return current; }
        public void setCurrent(double current) { this.current = current; }

        public double getPrevious() { return previous; }
        public void setPrevious(double previous) { this.previous = previous; }

        public double getChange() { return change; }
        public void setChange(double change) { this.change = change; }
    }

    public static class OutstandingMetric {
        private double total;
        private int count;

        public OutstandingMetric() {}
        public OutstandingMetric(double total, int count) { this.total = total; this.count = count; }

        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class CashFlowEntry {
        private String month;
        private double income;
        private double expense;

        public CashFlowEntry() {}
        public CashFlowEntry(String month, double income, double expense) { this.month = month; this.income = income; this.expense = expense; }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public double getIncome() { return income; }
        public void setIncome(double income) { this.income = income; }

        public double getExpense() { return expense; }
        public void setExpense(double expense) { this.expense = expense; }
    }

    // ── Cases ──
    public static class CaseReport {
        private int active;
        private int pending;
        private int closed;
        private int dismissed;
        private List<DistributionItem> courtDistribution;
        private List<DistributionItem> typeDistribution;

        public int getActive() { return active; }
        public void setActive(int active) { this.active = active; }

        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }

        public int getClosed() { return closed; }
        public void setClosed(int closed) { this.closed = closed; }

        public int getDismissed() { return dismissed; }
        public void setDismissed(int dismissed) { this.dismissed = dismissed; }

        public List<DistributionItem> getCourtDistribution() { return courtDistribution; }
        public void setCourtDistribution(List<DistributionItem> courtDistribution) { this.courtDistribution = courtDistribution; }

        public List<DistributionItem> getTypeDistribution() { return typeDistribution; }
        public void setTypeDistribution(List<DistributionItem> typeDistribution) { this.typeDistribution = typeDistribution; }
    }

    public static class DistributionItem {
        private String name;
        private long count;

        public DistributionItem() {}
        public DistributionItem(String name, long count) { this.name = name; this.count = count; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    // ── Clients ──
    public static class ClientReport {
        private Metric newClients;
        private List<GrowthEntry> growth;
        private OutstandingMetric pendingPayments;

        public Metric getNewClients() { return newClients; }
        public void setNewClients(Metric newClients) { this.newClients = newClients; }

        public List<GrowthEntry> getGrowth() { return growth; }
        public void setGrowth(List<GrowthEntry> growth) { this.growth = growth; }

        public OutstandingMetric getPendingPayments() { return pendingPayments; }
        public void setPendingPayments(OutstandingMetric pendingPayments) { this.pendingPayments = pendingPayments; }
    }

    public static class GrowthEntry {
        private String month;
        private long count;

        public GrowthEntry() {}
        public GrowthEntry(String month, long count) { this.month = month; this.count = count; }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    // ── Hearings ──
    public static class HearingReport {
        private int today;
        private int upcoming;
        private int missed;
        private List<DistributionItem> courtWise;

        public int getToday() { return today; }
        public void setToday(int today) { this.today = today; }

        public int getUpcoming() { return upcoming; }
        public void setUpcoming(int upcoming) { this.upcoming = upcoming; }

        public int getMissed() { return missed; }
        public void setMissed(int missed) { this.missed = missed; }

        public List<DistributionItem> getCourtWise() { return courtWise; }
        public void setCourtWise(List<DistributionItem> courtWise) { this.courtWise = courtWise; }
    }
}
