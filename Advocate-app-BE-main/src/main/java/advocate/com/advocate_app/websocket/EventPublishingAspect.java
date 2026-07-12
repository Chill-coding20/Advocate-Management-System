package advocate.com.advocate_app.websocket;

import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.entity.ClientPayment;
import advocate.com.advocate_app.entity.Document;
import advocate.com.advocate_app.entity.Expense;
import advocate.com.advocate_app.entity.Invoice;
import advocate.com.advocate_app.entity.CaseEventEntity;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.websocket.event.EventType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class EventPublishingAspect {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private AdvocateRepository advocateRepository;

    private String extractEmail(Object[] args) {
        if (args.length > 0 && args[0] instanceof String) {
            return (String) args[0];
        }
        return null;
    }

    // ─── Client ────────────────────────────────────────────────

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.ClientService.addClient(..))", returning = "result")
    public void onClientCreated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof Client)) return;
        Client client = (Client) result;
        String name = client.getName() != null ? client.getName() : "Unknown";
        webSocketService.sendNotification(email, EventType.CLIENT_CREATED,
                "Client " + name + " registered.", "Client", client.getId(), "/dashboard/clients");
        webSocketService.sendActivity(email, EventType.CLIENT_CREATED, "Client " + name + " registered.");
        webSocketService.sendDashboardUpdate(email);
        webSocketService.sendSearchUpdate(email, "Client", "CREATED", client);
    }

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.ClientService.updateClient(..))", returning = "result")
    public void onClientUpdated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof Client)) return;
        Client client = (Client) result;
        String name = client.getName() != null ? client.getName() : "Unknown";
        webSocketService.sendNotification(email, EventType.CLIENT_UPDATED,
                "Client " + name + " updated.", "Client", client.getId(), "/dashboard/clients");
        webSocketService.sendDashboardUpdate(email);
    }

    // ─── Case ──────────────────────────────────────────────────

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.CaseService.createCase(..))", returning = "result")
    public void onCaseCreated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof CaseEntity)) return;
        CaseEntity c = (CaseEntity) result;
        String num = c.getCaseNumber() != null ? c.getCaseNumber() : "Unknown";
        webSocketService.sendNotification(email, EventType.CASE_CREATED,
                "Case " + num + " created.", "Case", c.getId(), "/dashboard/cases");
        webSocketService.sendActivity(email, EventType.CASE_CREATED, "Case " + num + " created.");
        webSocketService.sendDashboardUpdate(email);
        webSocketService.sendSearchUpdate(email, "Case", "CREATED", c);
    }

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.CaseService.updateCase(..))", returning = "result")
    public void onCaseUpdated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof CaseEntity)) return;
        CaseEntity c = (CaseEntity) result;
        String num = c.getCaseNumber() != null ? c.getCaseNumber() : "Unknown";
        if ("CLOSED".equalsIgnoreCase(c.getStatus())) {
            webSocketService.sendNotification(email, EventType.CASE_CLOSED,
                    "Case " + num + " closed.", "Case", c.getId(), "/dashboard/cases");
            webSocketService.sendActivity(email, EventType.CASE_CLOSED, "Case " + num + " closed.");
        } else {
            webSocketService.sendNotification(email, EventType.CASE_UPDATED,
                    "Case " + num + " updated.", "Case", c.getId(), "/dashboard/cases");
        }
        webSocketService.sendDashboardUpdate(email);
        webSocketService.sendSearchUpdate(email, "Case", "UPDATED", c);
    }

    // ─── Expense ────────────────────────────────────────────────

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.ExpenseService.createExpense(..))", returning = "result")
    public void onExpenseCreated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof Expense)) return;
        Expense e = (Expense) result;
        String title = e.getTitle() != null ? e.getTitle() : "Unknown";
        String amount = e.getAmount() != null ? "\u20B9" + String.format("%.0f", e.getAmount()) : "";
        webSocketService.sendNotification(email, EventType.EXPENSE_CREATED,
                "Expense " + amount + " added (" + title + ").", "Expense", e.getId(), "/dashboard/expenses");
        webSocketService.sendActivity(email, EventType.EXPENSE_CREATED, "Expense " + amount + " added: " + title);
        webSocketService.sendDashboardUpdate(email);
    }

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.ExpenseService.updateExpense(..))", returning = "result")
    public void onExpenseUpdated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof Expense)) return;
        webSocketService.sendDashboardUpdate(email);
    }

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.ExpenseService.deleteExpense(..))")
    public void onExpenseDeleted(JoinPoint jp) {
        String email = extractEmail(jp.getArgs());
        if (email == null) return;
        webSocketService.sendDashboardUpdate(email);
    }

    // ─── Invoice ────────────────────────────────────────────────

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.InvoiceService.createInvoice(..))", returning = "result")
    public void onInvoiceCreated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof Invoice)) return;
        Invoice inv = (Invoice) result;
        String num = inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "Unknown";
        webSocketService.sendNotification(email, EventType.INVOICE_CREATED,
                "Invoice " + num + " generated.", "Invoice", inv.getId(), "/dashboard/invoices");
        webSocketService.sendActivity(email, EventType.INVOICE_CREATED, "Invoice " + num + " generated.");
        webSocketService.sendDashboardUpdate(email);
    }

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.InvoiceService.payInvoice(..))", returning = "result")
    public void onInvoicePaid(JoinPoint jp, Object result) {
        String email = null;
        if (result instanceof Invoice) {
            Invoice inv = (Invoice) result;
            if (inv.getAdvocate() != null) {
                email = inv.getAdvocate().getEmail();
            }
        }
        if (email == null) return;
        String num = ((Invoice) result).getInvoiceNumber() != null ? ((Invoice) result).getInvoiceNumber() : "Unknown";
        webSocketService.sendNotification(email, EventType.PAYMENT_RECEIVED,
                "Payment received for Invoice " + num + ".", "Invoice", ((Invoice) result).getId(), "/dashboard/invoices");
        webSocketService.sendActivity(email, EventType.PAYMENT_RECEIVED, "Payment received for Invoice " + num + ".");
        webSocketService.sendDashboardUpdate(email);
    }

    // ─── CaseEvent (Hearings) ───────────────────────────────────

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.CaseEventService.createEvent(..))", returning = "result")
    public void onHearingCreated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof CaseEventEntity)) return;
        CaseEventEntity ev = (CaseEventEntity) result;
        String title = ev.getTitle() != null ? ev.getTitle() : "Event";
        webSocketService.sendNotification(email, EventType.HEARING_CREATED,
                "Hearing \"" + title + "\" scheduled.", "Hearing", ev.getId(), "/dashboard/hearings");
        webSocketService.sendActivity(email, EventType.HEARING_CREATED, "Hearing \"" + title + "\" scheduled.");
        webSocketService.sendDashboardUpdate(email);
    }

    // ─── Document ───────────────────────────────────────────────

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.DocumentService.uploadDocument(..))", returning = "result")
    public void onDocumentUploaded(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof Document)) return;
        Document doc = (Document) result;
        String name = doc.getDocumentName() != null ? doc.getDocumentName() : "Unknown";
        webSocketService.sendNotification(email, EventType.DOCUMENT_UPLOADED,
                "Document \"" + name + "\" uploaded.", "Document", doc.getId(), "/dashboard/documents");
        webSocketService.sendActivity(email, EventType.DOCUMENT_UPLOADED, "Document \"" + name + "\" uploaded.");
        webSocketService.sendDashboardUpdate(email);
        webSocketService.sendSearchUpdate(email, "Document", "CREATED", doc);
    }

    // ─── Payment (ClientPaymentService.createPayment) ───────────

    @AfterReturning(pointcut = "execution(* advocate.com.advocate_app.service.ClientPaymentService.createPayment(..))", returning = "result")
    public void onPaymentCreated(JoinPoint jp, Object result) {
        String email = extractEmail(jp.getArgs());
        if (email == null || !(result instanceof ClientPayment)) return;
        ClientPayment p = (ClientPayment) result;
        String amount = p.getAmount() != null ? "\u20B9" + String.format("%.0f", p.getAmount()) : "";
        webSocketService.sendNotification(email, EventType.PAYMENT_RECEIVED,
                "Payment " + amount + " received.", "Payment", p.getId(), "/dashboard/invoices");
        webSocketService.sendActivity(email, EventType.PAYMENT_RECEIVED, "Payment " + amount + " received.");
        webSocketService.sendDashboardUpdate(email);
    }
}
