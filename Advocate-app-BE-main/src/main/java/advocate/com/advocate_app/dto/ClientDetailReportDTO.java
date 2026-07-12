package advocate.com.advocate_app.dto;

import java.util.List;

public class ClientDetailReportDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String registrationDate;
    private long totalCases;
    private long activeCases;
    private long closedCases;
    private long pendingCases;
    private List<String> documents;
    private List<PaymentEntry> recentPayments;

    public static class PaymentEntry {
        private String date;
        private double amount;
        private String mode;
        private String reference;
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    public long getTotalCases() { return totalCases; }
    public void setTotalCases(long totalCases) { this.totalCases = totalCases; }
    public long getActiveCases() { return activeCases; }
    public void setActiveCases(long activeCases) { this.activeCases = activeCases; }
    public long getClosedCases() { return closedCases; }
    public void setClosedCases(long closedCases) { this.closedCases = closedCases; }
    public long getPendingCases() { return pendingCases; }
    public void setPendingCases(long pendingCases) { this.pendingCases = pendingCases; }
    public List<String> getDocuments() { return documents; }
    public void setDocuments(List<String> documents) { this.documents = documents; }
    public List<PaymentEntry> getRecentPayments() { return recentPayments; }
    public void setRecentPayments(List<PaymentEntry> recentPayments) { this.recentPayments = recentPayments; }
}
