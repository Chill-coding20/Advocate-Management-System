package advocate.com.advocate_app.dto;

public class DashboardSummaryDTO {
    private long totalCases;
    private long activeCases;
    private long clients;
    private long upcomingHearings;
    private long pendingInvoices;

    public long getTotalCases() { return totalCases; }
    public void setTotalCases(long totalCases) { this.totalCases = totalCases; }

    public long getActiveCases() { return activeCases; }
    public void setActiveCases(long activeCases) { this.activeCases = activeCases; }

    public long getClients() { return clients; }
    public void setClients(long clients) { this.clients = clients; }

    public long getUpcomingHearings() { return upcomingHearings; }
    public void setUpcomingHearings(long upcomingHearings) { this.upcomingHearings = upcomingHearings; }

    public long getPendingInvoices() { return pendingInvoices; }
    public void setPendingInvoices(long pendingInvoices) { this.pendingInvoices = pendingInvoices; }
}
