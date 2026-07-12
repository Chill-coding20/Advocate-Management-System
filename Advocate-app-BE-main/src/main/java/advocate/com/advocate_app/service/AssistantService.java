package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.*;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssistantService {

    @Autowired private CaseRepository caseRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private CaseEventRepository caseEventRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private ClientPaymentRepository clientPaymentRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private DashboardService dashboardService;

    public AssistantResponse processQuery(String query, String currentRoute, Advocate advocate) {
        String clean = query.toLowerCase().trim();
        AssistantResponse resp = new AssistantResponse();

        // --- Intent detection (rule-based, replaceable with LLM later) ---
        if (matchesAny(clean, "open dashboard", "dashboard", "go to dashboard", "show dashboard")) {
            return pageResponse("OPEN_DASHBOARD", "Opening your Dashboard overview.", "/dashboard");
        }
        if (matchesAny(clean, "open cases", "cases", "go to cases", "show cases")) {
            return pageResponse("OPEN_CASES", "Opening Case Management.", "/dashboard/cases");
        }
        if (matchesAny(clean, "open clients", "clients", "go to clients", "show clients", "client list")) {
            return pageResponse("OPEN_CLIENTS", "Opening Client Directory.", "/dashboard/clients");
        }
        if (matchesAny(clean, "open expenses", "expenses", "go to expenses", "show expenses")) {
            return pageResponse("OPEN_EXPENSES", "Opening Expense Tracker.", "/dashboard/expenses");
        }
        if (matchesAny(clean, "open calendar", "calendar", "go to calendar", "open hearings")) {
            return pageResponse("OPEN_CALENDAR", "Opening Hearings Calendar.", "/dashboard/hearings");
        }
        if (matchesAny(clean, "open documents", "documents", "go to documents", "show documents")) {
            return pageResponse("OPEN_DOCUMENTS", "Opening Documents Panel.", "/dashboard/documents");
        }
        if (matchesAny(clean, "open invoices", "invoices", "go to invoices", "show invoices")) {
            return pageResponse("OPEN_INVOICES", "Opening Invoices Panel.", "/dashboard/invoices");
        }
        if (matchesAny(clean, "open settings", "settings", "go to settings")) {
            return pageResponse("OPEN_SETTINGS", "Opening Settings.", "/dashboard/settings");
        }
        if (matchesAny(clean, "open reports", "reports", "go to reports", "show reports")) {
            return pageResponse("OPEN_REPORTS", "Opening Reports & Analytics.", "/dashboard/reports");
        }

        // --- Show summary ---
        if (matchesAny(clean, "show dashboard summary", "dashboard summary", "summary", "overview")) {
            return buildSummaryResponse(advocate);
        }

        // --- Show today's hearings ---
        if (matchesAny(clean, "today. hearings", "today.s hearing", "hearings today", "today hearing",
                       "show today. hearing", "hearing today", "upcoming hearing today")) {
            return buildHearingsResponse(advocate, LocalDate.now(), LocalDate.now().plusDays(1),
                    "Today's Hearings", "OPEN_CALENDAR", "/dashboard/hearings");
        }

        // --- Show upcoming hearings ---
        if (matchesAny(clean, "upcoming hearings", "show upcoming hearings", "next hearings", "future hearings")) {
            return buildHearingsResponse(advocate, LocalDate.now(), LocalDate.now().plusDays(30),
                    "Upcoming Hearings", "OPEN_CALENDAR", "/dashboard/hearings");
        }

        // --- Show pending invoices ---
        if (matchesAny(clean, "pending invoices", "show pending invoices", "unpaid invoices", "overdue invoices")) {
            return buildPendingInvoicesResponse(advocate);
        }

        // --- Show today's expenses ---
        if (matchesAny(clean, "today. expenses", "today expense", "expenses today", "show today. expenses")) {
            return buildExpensesResponse(advocate, LocalDate.now(), LocalDate.now().plusDays(1),
                    "Today's Expenses");
        }

        // --- Show monthly expenses ---
        if (matchesAny(clean, "monthly expenses", "this month expenses", "expenses this month", "show monthly expenses")) {
            LocalDate start = LocalDate.now().withDayOfMonth(1);
            LocalDate end = LocalDate.now();
            return buildExpensesResponse(advocate, start, end, "This Month's Expenses");
        }

        // --- Show monthly income ---
        if (matchesAny(clean, "monthly income", "this month income", "income this month", "show monthly income",
                       "revenue this month", "monthly revenue")) {
            return buildIncomeResponse(advocate);
        }

        // --- Show active cases count / how many ---
        if (matchesAny(clean, "how many active cases", "active cases count", "number of active cases")) {
            long count = caseRepository.countByAdvocateAndStatusAndDeletedFalse(advocate, "Active");
            return answerResponse("There are **" + count + "** active cases currently.");
        }

        if (matchesAny(clean, "how many clients", "total clients", "number of clients", "client count")) {
            long count = clientRepository.countByAdvocate(advocate);
            return answerResponse("You have **" + count + "** clients registered.");
        }

        if (matchesAny(clean, "how many hearings today", "hearings count today", "number of hearings today")) {
            long count = caseEventRepository.countUpcomingForAdvocate(advocate, LocalDate.now(), LocalDate.now().plusDays(1));
            return answerResponse("There are **" + count + "** hearings scheduled for today.");
        }

        // --- Search client ---
        if (clean.startsWith("find client ") || clean.startsWith("search client ") || clean.startsWith("search for client ")) {
            String name = extractAfter(clean, "find client ", "search client ", "search for client ");
            return buildSearchClientResponse(advocate, name);
        }

        // --- Search case ---
        if (clean.startsWith("find case ") || clean.startsWith("search case ") || clean.startsWith("search for case ")) {
            String keyword = extractAfter(clean, "find case ", "search case ", "search for case ");
            return buildSearchCaseResponse(advocate, keyword);
        }

        // --- Search expense ---
        if (matchesAny(clean, "search expense", "find expense", "search expenses", "find expenses")) {
            String keyword = null;
            if (clean.startsWith("search expense ") || clean.startsWith("find expense ")) {
                keyword = extractAfter(clean, "search expense ", "find expense ");
            }
            return buildSearchExpenseResponse(advocate, keyword);
        }

        // --- Search invoice ---
        if (clean.startsWith("search invoice ") || clean.startsWith("find invoice ")) {
            String keyword = extractAfter(clean, "search invoice ", "find invoice ");
            return buildSearchInvoiceResponse(advocate, keyword);
        }

        // --- Search document ---
        if (matchesAny(clean, "search document", "find document", "search documents", "find documents")) {
            String keyword = null;
            if (clean.startsWith("search document ") || clean.startsWith("find document ")) {
                keyword = extractAfter(clean, "search document ", "find document ");
            }
            return buildSearchDocumentResponse(advocate, keyword);
        }

        // --- Search hearing ---
        if (matchesAny(clean, "search hearing", "find hearing", "search hearings", "find hearings")) {
            return buildHearingsResponse(advocate, LocalDate.now(), LocalDate.now().plusDays(30),
                    "Upcoming Hearings", "OPEN_CALENDAR", "/dashboard/hearings");
        }

        // --- Create modals ---
        if (matchesAny(clean, "create client", "add client", "new client", "register client")) {
            return modalResponse("CREATE_CLIENT", "Opening the New Client form.", "/dashboard/clients", "create-client");
        }
        if (matchesAny(clean, "create case", "add case", "new case", "register case")) {
            return modalResponse("CREATE_CASE", "Opening the New Case form.", "/dashboard/cases", "create-case");
        }
        if (matchesAny(clean, "create expense", "add expense", "new expense")) {
            return modalResponse("CREATE_EXPENSE", "Opening the Add Expense form.", "/dashboard/expenses", "create-expense");
        }
        if (matchesAny(clean, "create hearing", "add hearing", "schedule hearing", "new hearing")) {
            return modalResponse("CREATE_HEARING", "Opening the Add Hearing form.", "/dashboard/hearings", "create-hearing");
        }
        if (matchesAny(clean, "create invoice", "add invoice", "generate invoice", "new invoice")) {
            return modalResponse("CREATE_INVOICE", "Opening the Invoice generator.", "/dashboard/invoices", "create-invoice");
        }

        // --- Refresh dashboard ---
        if (matchesAny(clean, "refresh dashboard", "reload dashboard", "refresh")) {
            return pageResponse("REFRESH_DASHBOARD", "Refreshing Dashboard data.", "/dashboard");
        }

        // --- Unknown ---
        resp.setIntent("UNKNOWN");
        resp.setAction("ANSWER");
        resp.setMessage("I didn't understand that command. Try something like:\n\n• \"Open Cases\"\n• \"Show today's hearings\"\n• \"Find client Rahul\"\n• \"Create Client\"\n• \"How many active cases?\"\n• \"Dashboard summary\"");
        return resp;
    }

    // --- Helper: create page navigation response ---
    private AssistantResponse pageResponse(String intent, String message, String route) {
        AssistantResponse r = new AssistantResponse();
        r.setIntent(intent);
        r.setAction("OPEN_PAGE");
        r.setRoute(route);
        r.setMessage(message);
        return r;
    }

    // --- Helper: create modal open response ---
    private AssistantResponse modalResponse(String intent, String message, String route, String modal) {
        AssistantResponse r = new AssistantResponse();
        r.setIntent(intent);
        r.setAction("OPEN_MODAL");
        r.setRoute(route);
        r.setModalToOpen(modal);
        r.setMessage(message);
        return r;
    }

    // --- Helper: create answer-only response ---
    private AssistantResponse answerResponse(String message) {
        AssistantResponse r = new AssistantResponse();
        r.setIntent("ANSWER");
        r.setAction("ANSWER");
        r.setMessage(message);
        return r;
    }

    // --- Build dashboard summary ---
    private AssistantResponse buildSummaryResponse(Advocate advocate) {
        DashboardSummaryDTO summary = dashboardService.getSummary(advocate);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalCases", summary.getTotalCases());
        data.put("activeCases", summary.getActiveCases());
        data.put("clients", summary.getClients());
        data.put("upcomingHearings", summary.getUpcomingHearings());
        data.put("pendingInvoices", summary.getPendingInvoices());

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SHOW_SUMMARY");
        r.setAction("SHOW_DATA");
        r.setRoute("/dashboard");
        r.setMessage( String.format(
                "📊 **Dashboard Summary**\n\n• Total Cases: **%d**\n• Active Cases: **%d**\n• Clients: **%d**\n• Upcoming Hearings: **%d**\n• Pending Invoices: **%d**",
                summary.getTotalCases(), summary.getActiveCases(), summary.getClients(),
                summary.getUpcomingHearings(), summary.getPendingInvoices()));
        r.setData(data);
        return r;
    }

    // --- Build hearings response ---
    private AssistantResponse buildHearingsResponse(Advocate advocate, LocalDate start, LocalDate end,
                                                     String label, String intentRoute, String route) {
        List<CaseEventEntity> hearings = caseEventRepository.findUpcomingEvents(advocate, start, end).stream()
                .filter(e -> "HEARING".equalsIgnoreCase(e.getEventType()))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> results = hearings.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("title", h.getTitle());
            m.put("date", h.getDate() != null ? h.getDate().toString() : "");
            m.put("time", h.getTime() != null ? h.getTime() : "");
            m.put("caseNumber", h.getCaseEntity() != null ? h.getCaseEntity().getCaseNumber() : "N/A");
            m.put("clientName", h.getCaseEntity() != null && h.getCaseEntity().getClient() != null
                    ? h.getCaseEntity().getClient().getName() : "N/A");
            return m;
        }).collect(Collectors.toList());

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SHOW_HEARINGS");
        r.setAction("SHOW_DATA");
        r.setRoute(route);
        r.setMessage(results.isEmpty()
                ? "No " + label.toLowerCase() + " found."
                : "Found **" + results.size() + "** " + label.toLowerCase() + ".");
        r.setData(Map.of("label", label, "count", results.size()));
        r.setResults(results);
        return r;
    }

    // --- Build pending invoices response ---
    private AssistantResponse buildPendingInvoicesResponse(Advocate advocate) {
        List<Invoice> invoices = invoiceRepository.findByAdvocate(advocate).stream()
                .filter(i -> "UNPAID".equalsIgnoreCase(i.getStatus()) || "OVERDUE".equalsIgnoreCase(i.getStatus()))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> results = invoices.stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId());
            m.put("invoiceNumber", i.getInvoiceNumber());
            m.put("amount", i.getAmount());
            m.put("status", i.getStatus());
            m.put("clientName", i.getClient() != null ? i.getClient().getName() : "N/A");
            m.put("dueDate", i.getDueDate() != null ? i.getDueDate().toString() : "");
            return m;
        }).collect(Collectors.toList());

        double total = invoices.stream().mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0).sum();

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SHOW_PENDING_INVOICES");
        r.setAction("SHOW_DATA");
        r.setRoute("/dashboard/invoices");
        r.setMessage(results.isEmpty()
                ? "No pending invoices. Great job!"
                : "You have **" + results.size() + "** pending invoice(s) totaling **₹" + String.format("%.0f", total) + "**.");
        r.setResults(results);
        r.setData(Map.of("count", results.size(), "total", total));
        return r;
    }

    // --- Build expenses response ---
    private AssistantResponse buildExpensesResponse(Advocate advocate, LocalDate start, LocalDate end, String label) {
        List<Expense> expenses = expenseRepository.findByAdvocate(advocate).stream()
                .filter(e -> e.getPaymentDate() != null &&
                        !e.getPaymentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(start) &&
                        !e.getPaymentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(end))
                .limit(10)
                .collect(Collectors.toList());

        double total = expenses.stream().mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0).sum();

        List<Map<String, Object>> results = expenses.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitle());
            m.put("amount", e.getAmount());
            m.put("category", e.getCategory());
            m.put("date", e.getPaymentDate() != null ? e.getPaymentDate().toString() : "");
            return m;
        }).collect(Collectors.toList());

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SHOW_EXPENSES");
        r.setAction("SHOW_DATA");
        r.setRoute("/dashboard/expenses");
        r.setMessage(results.isEmpty()
                ? "No expenses found for " + label.toLowerCase() + "."
                : label + ": **" + results.size() + "** expense(s) totaling **₹" + String.format("%.0f", total) + "**.");
        r.setResults(results);
        r.setData(Map.of("label", label, "count", results.size(), "total", total));
        return r;
    }

    // --- Build monthly income response ---
    private AssistantResponse buildIncomeResponse(Advocate advocate) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        Double income = clientPaymentRepository.sumByAdvocateAndDateBetween(advocate, start, end);

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SHOW_MONTHLY_INCOME");
        r.setAction("ANSWER");
        r.setMessage("This month's revenue: **₹" + String.format("%.0f", income != null ? income : 0) + "**.");
        r.setData(Map.of("income", income != null ? income : 0, "month", start.getMonth().toString()));
        return r;
    }

    // --- Build search client response ---
    private AssistantResponse buildSearchClientResponse(Advocate advocate, String name) {
        List<Client> clients = clientRepository.findAllActiveByAdvocate(advocate).stream()
                .filter(c -> c.getName().toLowerCase().contains(name))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> results = clients.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("phone", c.getPhone());
            m.put("email", c.getEmail());
            return m;
        }).collect(Collectors.toList());

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SEARCH_CLIENT");
        r.setAction(results.size() == 1 ? "SEARCH" : "SHOW_DATA");
        r.setRoute("/dashboard/clients");
        r.setSearchQuery(name);
        r.setMessage(results.isEmpty()
                ? "No clients found matching **" + name + "**."
                : "Found **" + results.size() + "** client(s) matching **" + name + "**.");
        r.setResults(results);
        if (results.size() == 1) {
            r.setHighlightId(String.valueOf(results.get(0).get("id")));
        }
        return r;
    }

    // --- Build search case response ---
    private AssistantResponse buildSearchCaseResponse(Advocate advocate, String keyword) {
        List<CaseEntity> cases = caseRepository.findByAdvocate(advocate).stream()
                .filter(c -> !c.isDeleted() && (
                        c.getCaseNumber().toLowerCase().contains(keyword) ||
                        c.getCaseTitle().toLowerCase().contains(keyword) ||
                        c.getCaseType().toLowerCase().contains(keyword)))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> results = cases.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("caseNumber", c.getCaseNumber());
            m.put("caseTitle", c.getCaseTitle());
            m.put("status", c.getStatus());
            m.put("clientName", c.getClient() != null ? c.getClient().getName() : "N/A");
            return m;
        }).collect(Collectors.toList());

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SEARCH_CASE");
        r.setAction(results.size() == 1 ? "SEARCH" : "SHOW_DATA");
        r.setRoute("/dashboard/cases");
        r.setSearchQuery(keyword);
        r.setMessage(results.isEmpty()
                ? "No cases found matching **" + keyword + "**."
                : "Found **" + results.size() + "** case(s) matching **" + keyword + "**.");
        r.setResults(results);
        return r;
    }

    // --- Build search expense response ---
    private AssistantResponse buildSearchExpenseResponse(Advocate advocate, String keyword) {
        List<Expense> expenses;
        if (keyword != null && !keyword.isEmpty()) {
            expenses = expenseRepository.findByAdvocate(advocate).stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(keyword) ||
                                 e.getCategory().toLowerCase().contains(keyword))
                    .limit(10)
                    .collect(Collectors.toList());
        } else {
            expenses = expenseRepository.findByAdvocate(advocate).stream()
                    .limit(10)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> results = expenses.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("title", e.getTitle());
            m.put("amount", e.getAmount());
            m.put("category", e.getCategory());
            m.put("date", e.getPaymentDate() != null ? e.getPaymentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString() : "");
            return m;
        }).collect(Collectors.toList());

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SEARCH_EXPENSE");
        r.setAction("SHOW_DATA");
        r.setRoute("/dashboard/expenses");
        r.setMessage(results.isEmpty()
                ? "No expenses found."
                : "Found **" + results.size() + "** expense(s).");
        r.setResults(results);
        return r;
    }

    // --- Build search invoice response ---
    private AssistantResponse buildSearchInvoiceResponse(Advocate advocate, String keyword) {
        String kw = keyword.toLowerCase();
        List<Invoice> invoices = invoiceRepository.findByAdvocate(advocate).stream()
                .filter(i -> i.getInvoiceNumber().toLowerCase().contains(kw) ||
                             (i.getClient() != null && i.getClient().getName().toLowerCase().contains(kw)))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> results = invoices.stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId());
            m.put("invoiceNumber", i.getInvoiceNumber());
            m.put("amount", i.getAmount());
            m.put("status", i.getStatus());
            m.put("clientName", i.getClient() != null ? i.getClient().getName() : "N/A");
            return m;
        }).collect(Collectors.toList());

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SEARCH_INVOICE");
        r.setAction("SHOW_DATA");
        r.setRoute("/dashboard/invoices");
        r.setMessage(results.isEmpty()
                ? "No invoices found matching **" + keyword + "**."
                : "Found **" + results.size() + "** invoice(s).");
        r.setResults(results);
        return r;
    }

    // --- Build search document response ---
    private AssistantResponse buildSearchDocumentResponse(Advocate advocate, String keyword) {
        List<Document> docs;
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            docs = documentRepository.findByAdvocate(advocate).stream()
                    .filter(d -> d.getDocumentName().toLowerCase().contains(kw))
                    .limit(10)
                    .collect(Collectors.toList());
        } else {
            docs = documentRepository.findByAdvocate(advocate).stream()
                    .limit(10)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> results = docs.stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("fileName", d.getDocumentName());
            m.put("fileType", d.getFileType());
            m.put("caseNumber", d.getCaseEntity() != null ? d.getCaseEntity().getCaseNumber() : "N/A");
            return m;
        }).collect(Collectors.toList());

        AssistantResponse r = new AssistantResponse();
        r.setIntent("SEARCH_DOCUMENT");
        r.setAction("SHOW_DATA");
        r.setRoute("/dashboard/documents");
        r.setMessage(results.isEmpty()
                ? "No documents found."
                : "Found **" + results.size() + "** document(s).");
        r.setResults(results);
        return r;
    }

    // --- Utility: match any substring ---
    private boolean matchesAny(String clean, String... phrases) {
        for (String p : phrases) {
            if (clean.contains(p)) return true;
        }
        return false;
    }

    // --- Utility: extract keyword after any of the prefixes ---
    private String extractAfter(String clean, String... prefixes) {
        for (String p : prefixes) {
            if (clean.startsWith(p)) {
                return clean.substring(p.length()).trim();
            }
        }
        return "";
    }
}
