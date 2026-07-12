package advocate.com.advocate_app.dto;

import java.util.List;

public class SearchResponseDTO {

    private List<ClientResponseDTO> clients;
    private List<CaseResponseDTO> cases;
    private List<DocumentResponseDTO> documents;
    private List<InvoiceResponseDTO> invoices;
    private List<ExpenseResponseDTO> expenses;
    private List<TaskResponseDTO> tasks;
    private List<CaseEventResponseDTO> events;
    private List<ClientPaymentResponseDTO> payments;

    public SearchResponseDTO() {}

    public SearchResponseDTO(List<ClientResponseDTO> clients,
                             List<CaseResponseDTO> cases,
                             List<DocumentResponseDTO> documents,
                             List<InvoiceResponseDTO> invoices,
                             List<ExpenseResponseDTO> expenses,
                             List<TaskResponseDTO> tasks,
                             List<CaseEventResponseDTO> events,
                             List<ClientPaymentResponseDTO> payments) {
        this.clients = clients;
        this.cases = cases;
        this.documents = documents;
        this.invoices = invoices;
        this.expenses = expenses;
        this.tasks = tasks;
        this.events = events;
        this.payments = payments;
    }

    public List<ClientResponseDTO> getClients() { return clients; }
    public void setClients(List<ClientResponseDTO> clients) { this.clients = clients; }

    public List<CaseResponseDTO> getCases() { return cases; }
    public void setCases(List<CaseResponseDTO> cases) { this.cases = cases; }

    public List<DocumentResponseDTO> getDocuments() { return documents; }
    public void setDocuments(List<DocumentResponseDTO> documents) { this.documents = documents; }

    public List<InvoiceResponseDTO> getInvoices() { return invoices; }
    public void setInvoices(List<InvoiceResponseDTO> invoices) { this.invoices = invoices; }

    public List<ExpenseResponseDTO> getExpenses() { return expenses; }
    public void setExpenses(List<ExpenseResponseDTO> expenses) { this.expenses = expenses; }

    public List<TaskResponseDTO> getTasks() { return tasks; }
    public void setTasks(List<TaskResponseDTO> tasks) { this.tasks = tasks; }

    public List<CaseEventResponseDTO> getEvents() { return events; }
    public void setEvents(List<CaseEventResponseDTO> events) { this.events = events; }

    public List<ClientPaymentResponseDTO> getPayments() { return payments; }
    public void setPayments(List<ClientPaymentResponseDTO> payments) { this.payments = payments; }
}
