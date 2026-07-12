package advocate.com.advocate_app.communication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class EmailTemplateService {

    public static final String TEMPLATE_WELCOME_CLIENT     = "welcome_client";
    public static final String TEMPLATE_HEARING_REMINDER   = "hearing_reminder";
    public static final String TEMPLATE_CASE_CREATED       = "case_created";
    public static final String TEMPLATE_PAYMENT_RECEIVED   = "payment_received";
    public static final String TEMPLATE_INVOICE_GENERATED  = "invoice_generated";
    public static final String TEMPLATE_CASE_CLOSED        = "case_closed";

    public static final String TEMPLATE_HEARING_RESCHEDULED = "hearing_rescheduled";
    public static final String TEMPLATE_DOCUMENT_UPLOADED   = "document_uploaded";

    public static final String TEMPLATE_HELLO_WORLD        = "hello_world";

    @Value("${app.notification.sender.name:Advocate Case Management System}")
    private String systemName;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final String STYLES = ""
            + "*{margin:0;padding:0;box-sizing:border-box;}"
            + "body{font-family:'Segoe UI',Arial,Helvetica,sans-serif;background:#f0f2f5;margin:0;padding:0;-webkit-font-smoothing:antialiased;}"
            + ".wrapper{width:100%;padding:20px 10px;}"
            + ".container{max-width:650px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.06);}"
            + ".header{padding:36px 36px 24px;text-align:center;}"
            + ".icon-circle{display:inline-block;width:60px;height:60px;border-radius:50%;line-height:60px;font-size:28px;margin-bottom:14px;}"
            + ".header h1{color:#ffffff;font-size:22px;font-weight:700;margin:0;letter-spacing:0.3px;}"
            + ".header p{color:rgba(255,255,255,0.85);font-size:13px;margin:6px 0 0;}"
            + ".body{padding:28px 36px;color:#333333;line-height:1.8;font-size:15px;}"
            + ".body p{margin:0 0 16px;}"
            + ".info-card{width:100%;border-collapse:collapse;margin:20px 0;border-radius:12px;overflow:hidden;border:1px solid #e8ecf0;}"
            + ".info-card td{padding:14px 18px;font-size:14px;vertical-align:top;border-bottom:1px solid #f0f2f5;}"
            + ".info-card tr:last-child td{border-bottom:none;}"
            + ".info-card tr:nth-child(even) td{background:#f8f9fb;}"
            + ".info-card .label{font-weight:600;color:#1a237e;width:40%;white-space:nowrap;}"
            + ".info-card .value{color:#333333;}"
            + ".btn-wrap{text-align:center;margin:24px 0 8px;}"
            + ".btn{display:inline-block;padding:14px 36px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;border-radius:8px;}"
            + ".footer{background:#f8f9fb;padding:28px 36px;text-align:center;font-size:12px;color:#888888;border-top:1px solid #e8ecf0;line-height:1.8;}"
            + ".footer p{margin:4px 0;}"
            + ".footer .legal{margin-top:12px;padding-top:12px;border-top:1px solid #e8ecf0;}"
            + "@media only screen and (max-width:480px){"
            + ".wrapper{padding:10px 4px;}"
            + ".body{padding:20px 16px;}"
            + ".header{padding:24px 16px 16px;}"
            + ".footer{padding:20px 16px;}"
            + ".info-card td{display:block;width:100%;padding:10px 14px;}"
            + ".info-card .label{width:100%;background:#f0f2f7;}"
            + ".info-card tr:nth-child(even) .label{background:#e8ebf2;}"
            + ".container{border-radius:12px;}"
            + "}";

    // ==================== MASTER TEMPLATE BUILDER ====================

    private String buildTemplate(String title, String icon, String themePrimary, String themeLight,
                                  String greeting, String messageHtml, String infoCardHtml,
                                  String buttonText, String buttonUrl) {
        int year = Year.now().getValue();
        String iconBg = "background:" + themePrimary + "22;color:" + themePrimary + ";";
        String headerBg = "background:linear-gradient(135deg," + themePrimary + "," + themeLight + ");";
        String buttonBg = "background:linear-gradient(135deg," + themePrimary + "," + themeLight + ");";
        String button = "";
        if (buttonText != null && !buttonText.isBlank() && buttonUrl != null && !buttonUrl.isBlank()) {
            String finalUrl = buttonUrl.startsWith("http") ? buttonUrl : frontendUrl + buttonUrl;
            button = "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td class=\"btn-wrap\">"
                   + "<a href=\"" + escapeHtml(finalUrl) + "\" class=\"btn\" style=\"" + buttonBg + "display:inline-block;padding:14px 36px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;border-radius:8px;\">"
                   + escapeHtml(buttonText) + "</a>"
                   + "</td></tr></table>";
        }
        String infoCard = (infoCardHtml != null && !infoCardHtml.isBlank())
                ? infoCardHtml : "";
        return "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">"
                + "<!--[if mso]><xml><o:OfficeDocumentSettings><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml><![endif]-->"
                + "<style>" + STYLES + "</style></head>"
                + "<body>"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f0f2f5;\"><tr><td align=\"center\" style=\"padding:20px 10px;\">"
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" class=\"container\" style=\"max-width:650px;width:100%;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.06);\">"
                + "<tr><td class=\"header\" style=\"" + headerBg + "padding:36px 36px 24px;text-align:center;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\">"
                + "<div class=\"icon-circle\" style=\"" + iconBg + "display:inline-block;width:60px;height:60px;border-radius:50%;line-height:60px;font-size:28px;margin-bottom:14px;\">" + icon + "</div>"
                + "</td></tr><tr><td align=\"center\">"
                + "<h1 style=\"color:#ffffff;font-size:22px;font-weight:700;margin:0;letter-spacing:0.3px;\">" + escapeHtml(title) + "</h1>"
                + "</td></tr></table>"
                + "</td></tr>"
                + "<tr><td class=\"body\" style=\"padding:28px 36px;color:#333333;line-height:1.8;font-size:15px;\">"
                + "<p style=\"margin:0 0 16px;\">" + greeting + "</p>"
                + messageHtml
                + infoCard
                + button
                + "</td></tr>"
                + "<tr><td class=\"footer\" style=\"background:#f8f9fb;padding:28px 36px;text-align:center;font-size:12px;color:#888888;border-top:1px solid #e8ecf0;line-height:1.8;\">"
                + "<p style=\"margin:4px 0;\">This is an automated notification. Please do not reply to this email.</p>"
                + "<p style=\"margin:4px 0;\">If you have any questions, please contact your advocate directly.</p>"
                + "<div class=\"legal\" style=\"margin-top:12px;padding-top:12px;border-top:1px solid #e8ecf0;\">"
                + "<p style=\"margin:4px 0;\">&copy; " + year + " " + escapeHtml(systemName) + " &mdash; All Rights Reserved</p>"
                + "</div>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr></table>"
                + "</body></html>";
    }

    private String buildInfoCard(String[][] rows) {
        if (rows == null || rows.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" class=\"info-card\" style=\"width:100%;border-collapse:collapse;margin:20px 0;border-radius:12px;overflow:hidden;border:1px solid #e8ecf0;\">");
        for (String[] row : rows) {
            String label = row.length > 0 ? row[0] : "";
            String value = row.length > 1 ? row[1] : "";
            sb.append("<tr>");
            sb.append("<td class=\"label\" style=\"padding:14px 18px;font-size:14px;vertical-align:top;border-bottom:1px solid #f0f2f5;font-weight:600;color:#1a237e;width:40%;white-space:nowrap;\">").append(escapeHtml(label)).append("</td>");
            sb.append("<td class=\"value\" style=\"padding:14px 18px;font-size:14px;vertical-align:top;border-bottom:1px solid #f0f2f5;color:#333333;\">").append(value).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    // ==================== EMAIL TEMPLATES ====================

    // --- 1. CLIENT REGISTERED ---

    public String clientRegisteredEmail(String clientName, String advocateName) {
        return clientRegisteredEmail(clientName, advocateName, null);
    }

    public String clientRegisteredEmail(String clientName, String advocateName, String clientUrl) {
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>Welcome! You have been successfully registered as a client with <strong>" + escapeHtml(advocateName) + "</strong>.</p>"
                + "<p>Your advocate will reach out shortly regarding your case details.</p>"
                + "<p>Please keep this email for your records.</p>";
        String[][] infoRows = {
            {"Client Name", escapeHtml(clientName)},
            {"Registration Date", LocalDate.now().format(DATE_FMT)},
            {"Advocate", escapeHtml(advocateName)}
        };
        return buildTemplate("Client Registration Confirmed", "\uD83D\uDC64", "#059669", "#34d399",
                greeting, message, buildInfoCard(infoRows), "View Client", clientUrl);
    }

    // --- 2. CASE CREATED ---

    public String caseCreatedEmail(String clientName, String caseNumber, String caseTitle, String advocateName) {
        return caseCreatedEmail(clientName, caseNumber, caseTitle, advocateName, null);
    }

    public String caseCreatedEmail(String clientName, String caseNumber, String caseTitle, String advocateName, String caseUrl) {
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>A new case has been registered on your behalf.</p>";
        String[][] infoRows = {
            {"Case Number", escapeHtml(caseNumber)},
            {"Case Title", escapeHtml(caseTitle != null ? caseTitle : "")},
            {"Advocate", escapeHtml(advocateName)},
            {"Status", "<span style=\"color:#2563eb;font-weight:600;\">ACTIVE</span>"}
        };
        return buildTemplate("Case Successfully Registered", "\u2696\uFE0F", "#2563eb", "#60a5fa",
                greeting, message, buildInfoCard(infoRows), "View Case", caseUrl);
    }

    // --- 3. HEARING SCHEDULED ---

    public String hearingScheduledEmail(String clientName, String caseNumber, LocalDate date,
                                         LocalTime time, String court, String eventType) {
        return hearingScheduledEmail(clientName, caseNumber, date, time, court, eventType, null);
    }

    public String hearingScheduledEmail(String clientName, String caseNumber, LocalDate date,
                                         LocalTime time, String court, String eventType, String hearingUrl) {
        String formattedDate = date != null ? date.format(DATE_FMT) : "TBD";
        String formattedTime = time != null ? time.format(TIME_FMT) : "TBD";
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>A <strong>" + escapeHtml(eventType) + "</strong> has been scheduled for your case.</p>";
        String[][] infoRows = {
            {"Case Number", escapeHtml(caseNumber)},
            {"Court / Venue", escapeHtml(court != null ? court : "N/A")},
            {"Date", formattedDate},
            {"Time", formattedTime},
            {"Purpose", escapeHtml(eventType)}
        };
        return buildTemplate("Hearing Scheduled", "\uD83D\uDCC5", "#7c3aed", "#a78bfa",
                greeting, message, buildInfoCard(infoRows), "View Hearing", hearingUrl);
    }

    // --- 4. HEARING REMINDER ---

    public String hearingReminderEmail(String clientName, String caseNumber, LocalDate date,
                                        LocalTime time, String court) {
        return hearingReminderEmail(clientName, caseNumber, date, time, court, null);
    }

    public String hearingReminderEmail(String clientName, String caseNumber, LocalDate date,
                                        LocalTime time, String court, String hearingUrl) {
        String formattedDate = date != null ? date.format(DATE_FMT) : "TBD";
        String formattedTime = time != null ? time.format(TIME_FMT) : "TBD";
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String remainingTime = date != null ? computeRemainingText(date) : "";
        String message = "<p>This is a reminder that your next hearing is scheduled.</p>"
                + (remainingTime.isEmpty() ? "" : "<p style=\"font-size:16px;font-weight:600;color:#d97706;\">\u23F0 " + remainingTime + "</p>")
                + "<p style=\"color:#d97706;font-weight:500;\">Please arrive at least 15 minutes early.</p>";
        String[][] infoRows = {
            {"Case Number", escapeHtml(caseNumber)},
            {"Court", escapeHtml(court != null ? court : "N/A")},
            {"Date", formattedDate},
            {"Time", formattedTime}
        };
        return buildTemplate("Upcoming Hearing Reminder", "\u23F0", "#d97706", "#fbbf24",
                greeting, message, buildInfoCard(infoRows), "View Hearing", hearingUrl);
    }

    // --- 5. INVOICE GENERATED ---

    public String invoiceGeneratedEmail(String clientName, String invoiceNumber, String caseNumber,
                                         double amount, LocalDate dueDate) {
        return invoiceGeneratedEmail(clientName, invoiceNumber, caseNumber, amount, dueDate, null);
    }

    public String invoiceGeneratedEmail(String clientName, String invoiceNumber, String caseNumber,
                                         double amount, LocalDate dueDate, String invoiceUrl) {
        String formattedDue = dueDate != null ? dueDate.format(DATE_FMT) : "N/A";
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>An invoice has been generated for your case. Please review the details below.</p>";
        String[][] infoRows = {
            {"Invoice Number", escapeHtml(invoiceNumber)},
            {"Case Number", escapeHtml(caseNumber)},
            {"Amount", "<strong style=\"color:#4f46e5;font-size:16px;\">\u20B9" + String.format("%,.2f", amount) + "</strong>"},
            {"Due Date", formattedDue},
            {"Status", "<span style=\"color:#dc2626;font-weight:600;\">UNPAID</span>"}
        };
        String message2 = "<p>Please make the payment before the due date to avoid any inconvenience.</p>";
        return buildTemplate("Invoice Generated", "\uD83D\uDCB0", "#4f46e5", "#818cf8",
                greeting, message + message2, buildInfoCard(infoRows), "View Invoice", invoiceUrl);
    }

    // --- 6. PAYMENT RECEIVED ---

    public String paymentReceivedEmail(String clientName, String caseNumber, double amount, String referenceNumber) {
        return paymentReceivedEmail(clientName, caseNumber, amount, referenceNumber, null);
    }

    public String paymentReceivedEmail(String clientName, String caseNumber, double amount, String referenceNumber, String invoiceUrl) {
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>Thank you! Your payment has been received successfully.</p>";
        String[][] infoRows = {
            {"Amount Paid", "<strong style=\"color:#059669;font-size:16px;\">\u20B9" + String.format("%,.2f", amount) + "</strong>"},
            {"Case Number", escapeHtml(caseNumber)},
            {"Payment Date", LocalDate.now().format(DATE_FMT)},
            {"Reference No", escapeHtml(referenceNumber)},
            {"Status", "<span style=\"color:#059669;font-weight:600;\">PAID</span>"}
        };
        String message2 = "<p>Please keep this email as your payment receipt.</p>";
        return buildTemplate("Payment Received", "\u2705", "#059669", "#34d399",
                greeting, message + message2, buildInfoCard(infoRows), "View Invoice", invoiceUrl);
    }

    // --- 7. DOCUMENT UPLOADED ---

    public String documentUploadedEmail(String clientName, String caseNumber, String documentName, String category) {
        return documentUploadedEmail(clientName, caseNumber, documentName, category, null);
    }

    public String documentUploadedEmail(String clientName, String caseNumber, String documentName, String category, String documentUrl) {
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>A new document has been uploaded to your case.</p>";
        String[][] infoRows = {
            {"Document Name", escapeHtml(documentName)},
            {"Case Number", escapeHtml(caseNumber)},
            {"Category", escapeHtml(category != null ? category : "Other")},
            {"Upload Date", LocalDate.now().format(DATE_FMT)}
        };
        String message2 = "<p>Please log in to view or download the document.</p>";
        return buildTemplate("New Document Uploaded", "\uD83D\uDCC4", "#0d9488", "#2dd4bf",
                greeting, message + message2, buildInfoCard(infoRows), "Download Document", documentUrl);
    }

    // --- 8. CASE CLOSED ---

    public String caseClosedEmail(String clientName, String caseNumber, String caseTitle) {
        return caseClosedEmail(clientName, caseNumber, caseTitle, null);
    }

    public String caseClosedEmail(String clientName, String caseNumber, String caseTitle, String caseUrl) {
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>We are pleased to inform you that your case has been successfully closed.</p>"
                + "<p>Thank you for choosing our practice. We hope we served you well.</p>";
        String[][] infoRows = {
            {"Case Number", escapeHtml(caseNumber)},
            {"Case Title", escapeHtml(caseTitle != null ? caseTitle : "")},
            {"Closing Date", LocalDate.now().format(DATE_FMT)},
            {"Outcome", "<span style=\"color:#059669;font-weight:600;\">CLOSED</span>"}
        };
        return buildTemplate("Case Closed Successfully", "\u2696\uFE0F", "#059669", "#34d399",
                greeting, message, buildInfoCard(infoRows), "View Case", caseUrl);
    }

    // --- CASE STATUS UPDATED ---

    public String caseStatusUpdatedEmail(String clientName, String caseNumber, String oldStatus, String newStatus) {
        return caseStatusUpdatedEmail(clientName, caseNumber, oldStatus, newStatus, null);
    }

    public String caseStatusUpdatedEmail(String clientName, String caseNumber, String oldStatus, String newStatus, String caseUrl) {
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>The status of your case has been updated.</p>";
        String[][] infoRows = {
            {"Case Number", escapeHtml(caseNumber)},
            {"Previous Status", escapeHtml(oldStatus != null ? oldStatus : "N/A")},
            {"New Status", "<span style=\"color:#2563eb;font-weight:600;\">" + escapeHtml(newStatus != null ? newStatus : "") + "</span>"}
        };
        return buildTemplate("Case Status Updated", "\u2696\uFE0F", "#2563eb", "#60a5fa",
                greeting, message, buildInfoCard(infoRows), "View Case", caseUrl);
    }

    // --- HEARING RESCHEDULED ---

    public String hearingRescheduledEmail(String clientName, String caseNumber, String eventType,
                                           LocalDate oldDate, LocalTime oldTime,
                                           LocalDate newDate, LocalTime newTime, String court) {
        return hearingRescheduledEmail(clientName, caseNumber, eventType, oldDate, oldTime, newDate, newTime, court, null);
    }

    public String hearingRescheduledEmail(String clientName, String caseNumber, String eventType,
                                           LocalDate oldDate, LocalTime oldTime,
                                           LocalDate newDate, LocalTime newTime, String court, String hearingUrl) {
        String oldDateFmt = oldDate != null ? oldDate.format(DATE_FMT) : "TBD";
        String oldTimeFmt = oldTime != null ? oldTime.format(TIME_FMT) : "TBD";
        String newDateFmt = newDate != null ? newDate.format(DATE_FMT) : "TBD";
        String newTimeFmt = newTime != null ? newTime.format(TIME_FMT) : "TBD";
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>The following hearing has been rescheduled. Please update your calendar accordingly.</p>";
        String[][] infoRows = {
            {"Case Number", escapeHtml(caseNumber)},
            {"Event Type", escapeHtml(eventType)},
            {"Previous Date & Time", oldDateFmt + " at " + oldTimeFmt},
            {"New Date & Time", "<strong style=\"color:#d97706;\">" + newDateFmt + " at " + newTimeFmt + "</strong>"},
            {"Court / Venue", escapeHtml(court != null ? court : "N/A")}
        };
        return buildTemplate("Hearing Rescheduled", "\uD83D\uDCC5", "#d97706", "#fbbf24",
                greeting, message, buildInfoCard(infoRows), "View Hearing", hearingUrl);
    }

    // --- OVERDUE PAYMENT ---

    public String overduePaymentEmail(String clientName, String caseNumber, double pendingAmount) {
        return overduePaymentEmail(clientName, caseNumber, pendingAmount, null);
    }

    public String overduePaymentEmail(String clientName, String caseNumber, double pendingAmount, String invoiceUrl) {
        String greeting = "Dear <strong>" + escapeHtml(clientName) + "</strong>,";
        String message = "<p>This is a gentle reminder that you have an outstanding payment for your case.</p>";
        String[][] infoRows = {
            {"Case Number", escapeHtml(caseNumber)},
            {"Outstanding Amount", "<strong style=\"color:#dc2626;font-size:16px;\">\u20B9" + String.format("%,.2f", pendingAmount) + "</strong>"},
            {"Status", "<span style=\"color:#dc2626;font-weight:600;\">OVERDUE</span>"}
        };
        String message2 = "<p>Please make the payment at your earliest convenience to avoid any delays in your case proceedings.</p>";
        return buildTemplate("Payment Reminder", "\uD83D\uDCB0", "#dc2626", "#f87171",
                greeting, message + message2, buildInfoCard(infoRows), "View Invoice", invoiceUrl);
    }

    // --- TASK DEADLINE ---

    public String taskDeadlineEmail(String advocateName, String taskTitle, LocalDate deadline) {
        return taskDeadlineEmail(advocateName, taskTitle, deadline, null);
    }

    public String taskDeadlineEmail(String advocateName, String taskTitle, LocalDate deadline, String taskUrl) {
        String formattedDeadline = deadline != null ? deadline.format(DATE_FMT) : "N/A";
        String greeting = "Dear <strong>" + escapeHtml(advocateName) + "</strong>,";
        String message = "<p>This is a reminder about an upcoming task deadline.</p>"
                + "<p>Please ensure this task is completed before the deadline.</p>";
        String[][] infoRows = {
            {"Task", escapeHtml(taskTitle)},
            {"Deadline", "<strong style=\"color:#d97706;\">" + formattedDeadline + "</strong>"}
        };
        return buildTemplate("Task Deadline Reminder", "\u23F0", "#d97706", "#fbbf24",
                greeting, message, buildInfoCard(infoRows), "View Task", taskUrl);
    }

    // --- 9. PASSWORD RESET ---

    public String passwordResetEmail(String recipientName, String resetLink) {
        String greeting = "Dear <strong>" + escapeHtml(recipientName) + "</strong>,";
        String message = "<p>We received a request to reset your password for your Advocate Case Management System account.</p>"
                + "<p>Click the button below to reset your password. This link will expire in 60 minutes.</p>";
        String[][] infoRows = {};
        return buildTemplate("Password Reset Request", "\uD83D\uDD10", "#dc2626", "#f87171",
                greeting, message, "",
                "Reset Password", resetLink);
    }

    // --- 10. WEEKLY SUMMARY ---

    public String weeklySummaryEmail(String advocateName, Map<String, Object> stats) {
        String greeting = "Dear <strong>" + escapeHtml(advocateName) + "</strong>,";
        String message = "<p>Here is your weekly practice summary. Stay on top of your practice with these key metrics.</p>";

        long totalCases = getLongStat(stats, "totalCases");
        long totalHearings = getLongStat(stats, "totalHearings");
        long totalInvoices = getLongStat(stats, "totalInvoices");
        double totalRevenue = getDoubleStat(stats, "totalRevenue");
        double totalExpenses = getDoubleStat(stats, "totalExpenses");
        double pendingPayments = getDoubleStat(stats, "pendingPayments");

        String[][] infoRows = {
            {"Total Cases", String.valueOf(totalCases)},
            {"Total Hearings", String.valueOf(totalHearings)},
            {"Invoices Generated", String.valueOf(totalInvoices)},
            {"Revenue", "\u20B9" + String.format("%,.2f", totalRevenue)},
            {"Expenses", "\u20B9" + String.format("%,.2f", totalExpenses)},
            {"Pending Payments", "\u20B9" + String.format("%,.2f", pendingPayments)}
        };
        return buildTemplate("Weekly Practice Summary", "\uD83D\uDCCA", "#1e3a5f", "#3b82f6",
                greeting, message, buildInfoCard(infoRows),
                "View Dashboard", "/dashboard");
    }

    public String otpEmail(String recipientName, String otp, int expiryMinutes) {
        String greeting = "Dear <strong>" + escapeHtml(recipientName) + "</strong>,";
        String message = "<p>We received a request to reset your Advocate App password. Use the verification code below to proceed.</p>";

        String otpCard = "<div style=\"background:#f3f4f6;border-radius:12px;padding:24px;text-align:center;margin:20px 0;font-family:monospace,monospace;\">"
                + "<div style=\"font-size:11px;color:#6b7280;text-transform:uppercase;letter-spacing:1.5px;margin-bottom:8px;\">Verification Code</div>"
                + "<div style=\"font-size:36px;font-weight:700;color:#1e3a5f;letter-spacing:8px;\">" + escapeHtml(otp) + "</div>"
                + "</div>";

        String footerNote = "<p style=\"font-size:13px;color:#6b7280;\">This code expires in <strong>" + expiryMinutes + " minutes</strong>. If you did not request this, please ignore this email.</p>";

        return buildTemplate("Password Reset Request", "\uD83D\uDD12", "#dc2626", "#ef4444",
                greeting, message + otpCard + footerNote, null,
                null, null);
    }

    // ==================== WHATSAPP TEMPLATES ====================

    public String hearingReminderWhatsApp(String clientName, String caseNumber,
                                           LocalDate date, LocalTime time, String court) {
        String formattedDate = date != null ? date.format(DATE_FMT) : "TBD";
        String formattedTime = time != null ? time.format(TIME_FMT) : "TBD";
        return String.format(
                "Hello %s,\n\nYour hearing for Case %s is scheduled on %s at %s.\n\nCourt: %s\n\nPlease arrive 15 minutes early.\n\nRegards,\n%s",
                clientName, caseNumber, formattedDate, formattedTime,
                court != null ? court : "N/A", systemName);
    }

    public String caseCreatedWhatsApp(String clientName, String caseNumber, String caseTitle, String advocateName) {
        return String.format(
                "Hello %s,\n\nCase %s (%s) has been registered with Advocate %s.\n\nYour advocate will contact you soon.\n\nRegards,\n%s",
                clientName, caseNumber, caseTitle, advocateName, systemName);
    }

    public String invoiceGeneratedWhatsApp(String clientName, String invoiceNumber,
                                            String caseNumber, double amount, LocalDate dueDate) {
        String formattedDue = dueDate != null ? dueDate.format(DATE_FMT) : "N/A";
        return String.format(
                "Hello %s,\n\nInvoice %s has been generated for Case %s.\nAmount: \u20B9%,.2f\nDue Date: %s\n\nPlease make the payment on time.\n\nRegards,\n%s",
                clientName, invoiceNumber, caseNumber, amount, formattedDue, systemName);
    }

    public String paymentReceivedWhatsApp(String clientName, double amount, String caseNumber, String referenceNumber) {
        return String.format(
                "Thank you %s!\n\nPayment of \u20B9%,.2f received for Case %s.\nRef No: %s\n\nRegards,\n%s",
                clientName, amount, caseNumber, referenceNumber, systemName);
    }

    public String caseClosedWhatsApp(String clientName, String caseNumber, String caseTitle) {
        return String.format(
                "Hello %s,\n\nCase %s (%s) has been successfully closed.\n\nThank you for choosing our practice.\n\nRegards,\n%s",
                clientName, caseNumber, caseTitle, systemName);
    }

    public String hearingRescheduledWhatsApp(String clientName, String caseNumber,
                                              LocalDate oldDate, LocalTime oldTime,
                                              LocalDate newDate, LocalTime newTime, String court) {
        String oldDateFmt = oldDate != null ? oldDate.format(DATE_FMT) : "TBD";
        String oldTimeFmt = oldTime != null ? oldTime.format(TIME_FMT) : "TBD";
        String newDateFmt = newDate != null ? newDate.format(DATE_FMT) : "TBD";
        String newTimeFmt = newTime != null ? newTime.format(TIME_FMT) : "TBD";
        return String.format(
                "Hello %s,\n\nYour hearing for Case %s has been RESCHEDULED.\n\nPrevious: %s at %s\nNew Date: %s\nNew Time: %s\nCourt: %s\n\nRegards,\n%s",
                clientName, caseNumber, oldDateFmt, oldTimeFmt, newDateFmt, newTimeFmt,
                court != null ? court : "N/A", systemName);
    }

    public String documentUploadedWhatsApp(String clientName, String caseNumber, String documentName, String category) {
        return String.format(
                "Hello %s,\n\nA new document has been uploaded to Case %s.\n\nDocument: %s\nCategory: %s\n\nPlease log in to view it.\n\nRegards,\n%s",
                clientName, caseNumber, documentName, category != null ? category : "Other", systemName);
    }

    public String clientRegisteredWhatsApp(String clientName, String advocateName) {
        return String.format(
                "Hello %s,\n\nWelcome! You have been registered as a client with Advocate %s.\n\nYour advocate will contact you shortly.\n\nRegards,\n%s",
                clientName, advocateName, systemName);
    }

    public String overduePaymentWhatsApp(String clientName, String caseNumber, double pendingAmount) {
        return String.format(
                "Hello %s,\n\nThis is a gentle reminder about an outstanding payment of \u20B9%,.2f for Case %s.\n\nPlease clear the dues at the earliest.\n\nRegards,\n%s",
                clientName, pendingAmount, caseNumber, systemName);
    }

    // ==================== PRIVATE HELPERS ====================

    private String computeRemainingText(LocalDate date) {
        LocalDate today = LocalDate.now();
        long days = java.time.temporal.ChronoUnit.DAYS.between(today, date);
        if (days < 0) return "This hearing was scheduled for today or earlier.";
        if (days == 0) return "This hearing is scheduled for TODAY.";
        if (days == 1) return "This hearing is scheduled for TOMORROW.";
        if (days <= 7) return "This hearing is scheduled in " + days + " days.";
        return "This hearing is scheduled on " + date.format(DATE_FMT) + ".";
    }

    private long getLongStat(Map<String, Object> stats, String key) {
        if (stats == null || !stats.containsKey(key)) return 0;
        Object val = stats.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        return 0;
    }

    private double getDoubleStat(Map<String, Object> stats, String key) {
        if (stats == null || !stats.containsKey(key)) return 0.0;
        Object val = stats.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
