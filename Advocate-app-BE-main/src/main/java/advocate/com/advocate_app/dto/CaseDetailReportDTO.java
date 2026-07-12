package advocate.com.advocate_app.dto;

import java.util.List;

public class CaseDetailReportDTO {
    private Long id;
    private String caseNumber;
    private String caseTitle;
    private String caseType;
    private String courtLevel;
    private String clientName;
    private String status;
    private String filedDate;
    private String nextHearing;
    private String description;
    private String advocateName;
    private double totalExpenses;
    private double totalPayments;
    private List<InvoiceEntry> invoices;
    private List<String> documents;
    private List<TimelineEntry> timeline;

    public static class InvoiceEntry {
        private String number;
        private double amount;
        private String status;
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class TimelineEntry {
        private String date;
        private String event;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public String getCaseTitle() { return caseTitle; }
    public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }
    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }
    public String getCourtLevel() { return courtLevel; }
    public void setCourtLevel(String courtLevel) { this.courtLevel = courtLevel; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFiledDate() { return filedDate; }
    public void setFiledDate(String filedDate) { this.filedDate = filedDate; }
    public String getNextHearing() { return nextHearing; }
    public void setNextHearing(String nextHearing) { this.nextHearing = nextHearing; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAdvocateName() { return advocateName; }
    public void setAdvocateName(String advocateName) { this.advocateName = advocateName; }
    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }
    public double getTotalPayments() { return totalPayments; }
    public void setTotalPayments(double totalPayments) { this.totalPayments = totalPayments; }
    public List<InvoiceEntry> getInvoices() { return invoices; }
    public void setInvoices(List<InvoiceEntry> invoices) { this.invoices = invoices; }
    public List<String> getDocuments() { return documents; }
    public void setDocuments(List<String> documents) { this.documents = documents; }
    public List<TimelineEntry> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineEntry> timeline) { this.timeline = timeline; }
}
