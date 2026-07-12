package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.communication.service.CommunicationDispatcher;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.CaseRepository;
import advocate.com.advocate_app.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private ClientPaymentService clientPaymentService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private WhatsAppService whatsAppService; // Preserved existing interface

    @Autowired
    private CommunicationDispatcher notificationDispatcher;

    @Autowired
    private EmailTemplateService templateService;

    @Autowired
    private CaseTimelineService timelineService;

    @Autowired
    private AuditLogService auditLogService;

    public Invoice createInvoice(String email, Invoice invoice) {
        if (invoice.getCaseEntity() == null || invoice.getCaseEntity().getId() == null) {
            throw new RuntimeException("Case ID is required.");
        }

        CaseEntity caseEntity = caseRepository.findById(invoice.getCaseEntity().getId())
                .orElseThrow(() -> new RuntimeException("Case not found."));

        Advocate advocate = caseEntity.getAdvocate();
        if (!advocate.getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized.");
        }

        invoice.setCaseEntity(caseEntity);
        invoice.setClient(caseEntity.getClient());
        invoice.setAdvocate(advocate);

        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDate.now());
        }
        if (invoice.getDueDate() == null) {
            invoice.setDueDate(invoice.getInvoiceDate().plusDays(30)); // 30-day default
        }

        // Auto-generate invoice number if empty
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        }

        Invoice saved = invoiceRepository.save(invoice);

        // Log activity
        activityService.logActivity("Invoice " + saved.getInvoiceNumber() + " created", "INVOICE_CREATED", advocate);

        // ✅ New: Send email + WhatsApp notifications
        try {
            Client client = saved.getClient();
            if (client != null) {
                Map<String, String> templateParams = new HashMap<>();
                templateParams.put("1", client.getName());
                templateParams.put("2", saved.getInvoiceNumber());
                templateParams.put("3", caseEntity.getCaseNumber());
                templateParams.put("4", String.format("%.2f", saved.getAmount()));

                NotificationPayload payload = new NotificationPayload();
                payload.setType(NotificationType.INVOICE_GENERATED);
                payload.setRecipientName(client.getName());
                payload.setRecipientEmail(client.getEmail());
                payload.setRecipientPhone(client.getPhone());
                payload.setAdvocate(advocate);
                payload.setCaseEntity(caseEntity);
                payload.setClient(client);
                payload.setSubject("Invoice Generated — " + saved.getInvoiceNumber());
                payload.setEmailBody(templateService.invoiceGeneratedEmail(client.getName(),
                        saved.getInvoiceNumber(), caseEntity.getCaseNumber(),
                        saved.getAmount(), saved.getDueDate(), "/invoices/" + saved.getId()));
                payload.setWhatsappMessage(templateService.invoiceGeneratedWhatsApp(client.getName(),
                        saved.getInvoiceNumber(), caseEntity.getCaseNumber(),
                        saved.getAmount(), saved.getDueDate()));
                payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_INVOICE_GENERATED);
                payload.setWhatsappTemplateParameters(templateParams);
                notificationDispatcher.dispatchSafely(payload);
            }
        } catch (Exception e) {
            log.warn("Could not dispatch INVOICE_GENERATED notification: {}", e.getMessage());
        }

        // Legacy WhatsApp mock (preserved)
        try {
            whatsAppService.sendInvoiceGeneratedMessage(saved);
        } catch (Exception ex) {
            log.warn("Legacy WhatsApp mock error: {}", ex.getMessage());
        }

        // Record timeline
        try {
            String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String amount = saved.getAmount() != null ? String.format("₹%.0f", saved.getAmount()) : "";
            timelineService.recordEvent(
                    caseEntity.getId(), caseEntity.getClient() != null ? caseEntity.getClient().getId() : null,
                    advocate.getId(), CaseTimelineService.INVOICE_GENERATED,
                    "Invoice Generated",
                    "Invoice " + saved.getInvoiceNumber() + " for " + amount + " generated",
                    performedBy, "Invoice", saved.getId()
            );
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String auditAmount = saved.getAmount() != null ? String.format("₹%.0f", saved.getAmount()) : "";
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.INVOICE_GENERATED, AuditLogService.MODULE_INVOICES,
                    "Invoice Generated",
                    "Invoice " + saved.getInvoiceNumber() + " for " + auditAmount + " generated",
                    "Invoice", saved.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        return saved;
    }

    public Page<Invoice> getInvoicesPaged(String email, Pageable pageable) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found"));
        Page<Invoice> invoices = invoiceRepository.findByAdvocate(advocate, pageable);
        LocalDate today = LocalDate.now();
        boolean changed = false;
        for (Invoice inv : invoices.getContent()) {
            if ("UNPAID".equals(inv.getStatus()) && inv.getDueDate().isBefore(today)) {
                inv.setStatus("OVERDUE");
                changed = true;
            }
        }
        if (changed) invoiceRepository.saveAll(invoices.getContent());
        return invoices;
    }

    public List<Invoice> getMyInvoices(Advocate advocate) {
        // Automatically check and update overdue statuses
        List<Invoice> invoices = invoiceRepository.findByAdvocate(advocate);
        LocalDate today = LocalDate.now();
        boolean changed = false;
        for (Invoice inv : invoices) {
            if ("UNPAID".equals(inv.getStatus()) && inv.getDueDate().isBefore(today)) {
                inv.setStatus("OVERDUE");
                invoiceRepository.save(inv);
                changed = true;
            }
        }
        if (changed) {
            invoices = invoiceRepository.findByAdvocate(advocate);
        }
        return invoices;
    }

    public Invoice payInvoice(Long invoiceId, String email) {
        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found."));
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found."));

        if (!invoice.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to pay this invoice");
        }

        if ("PAID".equals(invoice.getStatus())) {
            return invoice;
        }

        invoice.setStatus("PAID");
        Invoice saved = invoiceRepository.save(invoice);

        // Record a ClientPayment to update financials
        ClientPayment payment = new ClientPayment();
        payment.setAmount(invoice.getAmount());
        payment.setPaymentMode("UPI");
        payment.setReferenceNumber("REF-" + System.currentTimeMillis());
        payment.setPaymentDate(new Date());
        payment.setDescription("Payment for Invoice " + invoice.getInvoiceNumber());
        payment.setCaseEntity(invoice.getCaseEntity());
        payment.setClient(invoice.getClient());
        payment.setAdvocate(invoice.getAdvocate());
        clientPaymentService.createPayment(invoice.getAdvocate().getEmail(), payment);

        // Log activity
        activityService.logActivity("Invoice " + invoice.getInvoiceNumber() + " marked as Paid", "PAYMENT_RECEIVED", invoice.getAdvocate());

        // ✅ New: Send email + WhatsApp notifications
        try {
            Client client = saved.getClient();
            if (client != null) {
                Map<String, String> templateParams = new HashMap<>();
                templateParams.put("1", client.getName());
                templateParams.put("2", String.format("%.2f", saved.getAmount()));
                templateParams.put("3", saved.getCaseEntity().getCaseNumber());
                templateParams.put("4", payment.getReferenceNumber());

                NotificationPayload payload = new NotificationPayload();
                payload.setType(NotificationType.PAYMENT_RECEIVED);
                payload.setRecipientName(client.getName());
                payload.setRecipientEmail(client.getEmail());
                payload.setRecipientPhone(client.getPhone());
                payload.setAdvocate(saved.getAdvocate());
                payload.setCaseEntity(saved.getCaseEntity());
                payload.setClient(client);
                payload.setSubject("Payment Received — ₹" + String.format("%,.2f", saved.getAmount()));
                payload.setEmailBody(templateService.paymentReceivedEmail(client.getName(),
                        saved.getCaseEntity().getCaseNumber(), saved.getAmount(),
                        payment.getReferenceNumber(), "/invoices/" + saved.getId()));
                payload.setWhatsappMessage(templateService.paymentReceivedWhatsApp(client.getName(),
                        saved.getAmount(), saved.getCaseEntity().getCaseNumber(),
                        payment.getReferenceNumber()));
                payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_PAYMENT_RECEIVED);
                payload.setWhatsappTemplateParameters(templateParams);
                notificationDispatcher.dispatchSafely(payload);
            }
        } catch (Exception e) {
            log.warn("Could not dispatch PAYMENT_RECEIVED notification: {}", e.getMessage());
        }

        // Legacy WhatsApp mock (preserved)
        try {
            whatsAppService.sendPaymentReceivedMessage(payment);
        } catch (Exception ex) {
            log.warn("Legacy WhatsApp mock error: {}", ex.getMessage());
        }

        // Record timeline
        try {
            CaseEntity invCase = saved.getCaseEntity();
            String performedBy = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String amount = saved.getAmount() != null ? String.format("₹%.0f", saved.getAmount()) : "";
            timelineService.recordEvent(
                    invCase != null ? invCase.getId() : null,
                    invCase != null && invCase.getClient() != null ? invCase.getClient().getId() : null,
                    advocate.getId(), CaseTimelineService.INVOICE_PAID,
                    "Invoice Paid",
                    "Invoice " + saved.getInvoiceNumber() + " — " + amount + " paid",
                    performedBy, "Invoice", saved.getId()
            );
        } catch (Exception e) {
            log.warn("Could not record timeline event: {}", e.getMessage());
        }

        // Record audit log (DO NOT record PAYMENT_RECEIVED here — it's recorded by clientPaymentService.createPayment)
        try {
            String auditUserName = advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail();
            String auditAmount = saved.getAmount() != null ? String.format("₹%.0f", saved.getAmount()) : "";
            auditLogService.recordAction(
                    advocate.getId(), auditUserName,
                    AuditLogService.INVOICE_PAID, AuditLogService.MODULE_INVOICES,
                    "Invoice Paid",
                    "Invoice " + saved.getInvoiceNumber() + " — " + auditAmount + " paid",
                    "Invoice", saved.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        return saved;
    }

    /**
     * Creates an initial unpaid invoice for a newly created case.
     * Does NOT send notifications, log activities, or trigger WhatsApp messages.
     * Skips if an invoice already exists for this case.
     *
     * @param caseEntity the newly created case
     * @return the saved invoice, or null if no amount was set or invoice already exists
     */
    public Invoice createInitialInvoiceForCase(CaseEntity caseEntity) {
        Advocate advocate = caseEntity.getAdvocate();

        // Determine amount: prefer totalClientAgreedAmount, fall back to amount
        Double invoiceAmount = caseEntity.getTotalClientAgreedAmount();
        if (invoiceAmount == null || invoiceAmount <= 0) {
            invoiceAmount = caseEntity.getAmount();
        }
        if (invoiceAmount == null || invoiceAmount <= 0) {
            return null;
        }

        // Prevent duplicate invoices for the same case
        List<Invoice> existing = invoiceRepository.findByCaseEntityAndAdvocate(caseEntity, advocate);
        if (!existing.isEmpty()) {
            log.info("Initial invoice skipped — invoice already exists for case {}", caseEntity.getCaseNumber());
            return null;
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setAmount(invoiceAmount);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus("UNPAID");
        invoice.setCaseEntity(caseEntity);
        invoice.setClient(caseEntity.getClient());
        invoice.setAdvocate(advocate);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Initial invoice {} created for case {} (amount={}, due={})",
                saved.getInvoiceNumber(), caseEntity.getCaseNumber(), invoiceAmount, saved.getDueDate());
        return saved;
    }

    public Map<String, Object> getInvoiceSummary(Advocate advocate) {
        // Refresh overdue statuses first
        getMyInvoices(advocate);

        Double paidSum = invoiceRepository.sumAmountByAdvocateAndStatus(advocate, "PAID");
        Double unpaidSum = invoiceRepository.sumAmountByAdvocateAndStatus(advocate, "UNPAID");
        Double overdueSum = invoiceRepository.sumAmountByAdvocateAndStatus(advocate, "OVERDUE");

        double paid = Optional.ofNullable(paidSum).orElse(0.0);
        double unpaid = Optional.ofNullable(unpaidSum).orElse(0.0);
        double overdue = Optional.ofNullable(overdueSum).orElse(0.0);

        return Map.of(
                "paid", paid,
                "unpaid", unpaid,
                "overdue", overdue,
                "monthlyRevenue", paid
        );
    }
}
