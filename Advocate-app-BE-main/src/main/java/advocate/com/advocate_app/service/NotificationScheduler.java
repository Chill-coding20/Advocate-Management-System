package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.communication.service.CommunicationDispatcher;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Daily notification scheduler.
 *
 * Runs at 7:00 AM every day and:
 *   1. Creates in-app hearing reminders (existing behavior - preserved)
 *   2. Sends email + WhatsApp hearing reminders to clients
 *   3. Checks overdue invoices and sends payment reminders
 *   4. Checks upcoming task deadlines and sends advocate reminders
 */
@Service
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    @Autowired
    private CaseEventRepository caseEventRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private CommunicationDispatcher notificationDispatcher;

    @Autowired
    private EmailTemplateService templateService;

    /**
     * Runs automatically at 7:00 AM every day.
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void generateDailyNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate twoDaysLater = today.plusDays(2);

        log.info("🔔 [Scheduler] Daily notification run started for: {}", today);

        checkHearingReminders(today, twoDaysLater);
        checkOverdueInvoices(today);
        checkTaskDeadlines(today, twoDaysLater);

        log.info("✅ [Scheduler] Daily notification run completed.");
    }

    // ==================== HEARING REMINDERS ====================

    private void checkHearingReminders(LocalDate today, LocalDate rangeEnd) {
        List<CaseEventEntity> events = caseEventRepository.findEventsForTodayAndTomorrow(today, rangeEnd);

        for (CaseEventEntity event : events) {
            try {
                if (event.isNotified()) continue; // Skip already-notified events

                LocalDate eventDate = event.getDate();
                Advocate advocate = event.getAdvocate();
                CaseEntity caseEntity = event.getCaseEntity();

                String message;
                if (eventDate.isEqual(today)) {
                    message = "Reminder: " + event.getEventType() + " today for Case "
                            + caseEntity.getCaseNumber()
                            + " (" + (event.getTime() != null ? event.getTime() : "Time not set") + ")";
                } else if (eventDate.isEqual(today.plusDays(1))) {
                    message = "Upcoming: " + event.getEventType() + " tomorrow for Case "
                            + caseEntity.getCaseNumber()
                            + " (" + (event.getTime() != null ? event.getTime() : "Time not set") + ")";
                } else {
                    message = "Upcoming: " + event.getEventType() + " on " + eventDate + " for Case "
                            + caseEntity.getCaseNumber()
                            + " (" + (event.getTime() != null ? event.getTime() : "Time not set") + ")";
                }

                // ✅ Create in-app notification (preserved existing behavior)
                NotificationEntity notification = new NotificationEntity();
                notification.setAdvocate(advocate);
                notification.setMessage(message);
                notification.setReadStatus(false);
                notification.setCreatedAt(java.time.LocalDateTime.now());
                notificationRepository.save(notification);

                // ✅ Send email + WhatsApp to client
                Client client = caseEntity.getClient();
                if (client != null) {
                    String formattedDate = event.getDate() != null ? event.getDate().toString() : "TBD";
                    String formattedTime = event.getTime() != null ? event.getTime().toString() : "TBD";
                    Map<String, String> templateParams = new HashMap<>();
                    templateParams.put("1", client.getName());
                    templateParams.put("2", caseEntity.getCaseNumber());
                    templateParams.put("3", formattedDate);
                    templateParams.put("4", formattedTime);

                    NotificationPayload payload = new NotificationPayload();
                    payload.setType(NotificationType.HEARING_REMINDER);
                    payload.setRecipientName(client.getName());
                    payload.setRecipientEmail(client.getEmail());
                    payload.setRecipientPhone(client.getPhone());
                    payload.setAdvocate(advocate);
                    payload.setCaseEntity(caseEntity);
                    payload.setClient(client);
                    payload.setSubject("Case Hearing Reminder — " + caseEntity.getCaseNumber());
                    payload.setEmailBody(templateService.hearingReminderEmail(client.getName(),
                            caseEntity.getCaseNumber(), event.getDate(), event.getTime(),
                            caseEntity.getCourtLevel(), "/cases/" + caseEntity.getId()));
                    payload.setWhatsappMessage(templateService.hearingReminderWhatsApp(client.getName(),
                            caseEntity.getCaseNumber(), event.getDate(), event.getTime(),
                            caseEntity.getCourtLevel()));
                    payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_HEARING_REMINDER);
                    payload.setWhatsappTemplateParameters(templateParams);
                    notificationDispatcher.dispatchSafely(payload);
                }

                // ✅ Mark as notified to prevent duplicate reminders
                event.setNotified(true);
                caseEventRepository.save(event);

                log.info("✅ Hearing reminder sent for advocate: {} | {}", advocate.getEmail(), message);

            } catch (Exception ex) {
                log.error("⚠️ Error processing hearing event ID: {} — {}", event.getId(), ex.getMessage());
            }
        }
    }

    // ==================== OVERDUE INVOICE REMINDERS ====================

    private void checkOverdueInvoices(LocalDate today) {
        List<Advocate> allAdvocates = advocateRepository.findAll();

        for (Advocate advocate : allAdvocates) {
            try {
                List<Invoice> overdueInvoices = invoiceRepository.findByAdvocateAndStatus(advocate, "OVERDUE");

                for (Invoice invoice : overdueInvoices) {
                    Client client = invoice.getClient();
                    if (client == null) continue;

                    // Calculate pending amount
                    double pending = invoice.getAmount() != null ? invoice.getAmount() : 0;

                    Map<String, String> templateParams = new HashMap<>();
                    templateParams.put("1", client.getName());
                    templateParams.put("2", invoice.getCaseEntity().getCaseNumber());
                    templateParams.put("3", String.format("%.2f", pending));

                    NotificationPayload payload = new NotificationPayload();
                    payload.setType(NotificationType.OVERDUE_PAYMENT_REMINDER);
                    payload.setRecipientName(client.getName());
                    payload.setRecipientEmail(client.getEmail());
                    payload.setRecipientPhone(client.getPhone());
                    payload.setAdvocate(advocate);
                    payload.setCaseEntity(invoice.getCaseEntity());
                    payload.setClient(client);
                    payload.setSubject("Payment Reminder — Case " + invoice.getCaseEntity().getCaseNumber());
                    payload.setEmailBody(templateService.overduePaymentEmail(client.getName(),
                            invoice.getCaseEntity().getCaseNumber(), pending, "/invoices/" + invoice.getId()));
                    payload.setWhatsappMessage(templateService.overduePaymentWhatsApp(client.getName(),
                            invoice.getCaseEntity().getCaseNumber(), pending));
                    payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_INVOICE_GENERATED);
                    payload.setWhatsappTemplateParameters(templateParams);
                    notificationDispatcher.dispatchSafely(payload);

                    log.info("💳 Overdue payment reminder sent for invoice: {} | client: {}",
                            invoice.getInvoiceNumber(), client.getName());
                }
            } catch (Exception e) {
                log.error("⚠️ Error checking overdue invoices for advocate: {} — {}", advocate.getEmail(), e.getMessage());
            }
        }
    }

    // ==================== TASK DEADLINE REMINDERS ====================

    private void checkTaskDeadlines(LocalDate today, LocalDate rangeEnd) {
        List<Advocate> allAdvocates = advocateRepository.findAll();

        for (Advocate advocate : allAdvocates) {
            try {
                List<Task> upcomingTasks = taskRepository.findUpcomingIncompleteTasksForAdvocate(
                        advocate, today, rangeEnd);

                for (Task task : upcomingTasks) {
                    // Send to advocate's email (not client)
                    NotificationPayload payload = new NotificationPayload();
                    payload.setType(NotificationType.TASK_DEADLINE_REMINDER);
                    payload.setRecipientName(advocate.getFullName());
                    payload.setRecipientEmail(advocate.getEmail());
                    payload.setRecipientPhone(advocate.getPhone());
                    payload.setAdvocate(advocate);
                    payload.setSubject("Task Deadline Reminder — " + task.getTitle());
                    payload.setEmailBody(templateService.taskDeadlineEmail(advocate.getFullName(),
                            task.getTitle(), task.getDeadline(), "/tasks/" + task.getId()));
                    payload.setWhatsappMessage(null); // No WhatsApp for task reminders
                    notificationDispatcher.dispatchSafely(payload);

                    log.info("📋 Task deadline reminder sent for advocate: {} | task: {}",
                            advocate.getEmail(), task.getTitle());
                }
            } catch (Exception e) {
                log.error("⚠️ Error checking task deadlines for advocate: {} — {}", advocate.getEmail(), e.getMessage());
            }
        }
    }
}
