package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.CaseDetailReportDTO;
import advocate.com.advocate_app.dto.ClientDetailReportDTO;
import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.entity.Expense;
import advocate.com.advocate_app.entity.Invoice;
import advocate.com.advocate_app.entity.ClientPayment;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfGeneratorService {

    private static final Color PRIMARY = new Color(15, 23, 42);
    private static final Color ACCENT = new Color(59, 130, 246);
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY = new Color(100, 116, 139);
    private static final Color LIGHT_BG = new Color(248, 250, 252);
    private static final Color ROW_ALT = new Color(241, 245, 249);

    private final Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, PRIMARY);
    private final Font headingFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY);
    private final Font subTitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY);
    private final Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, WHITE);
    private final Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, PRIMARY);
    private final Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, PRIMARY);
    private final Font smallFont = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY);
    private final Font footerFont = new Font(Font.HELVETICA, 7, Font.NORMAL, GRAY);
    private final Font watermarkFont = new Font(Font.HELVETICA, 48, Font.BOLD, new Color(200, 200, 200, 60));

    private void addHeader(Document doc, Advocate advocate, String reportTitle) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        Paragraph logo = new Paragraph("ADVOCATE APP", new Font(Font.HELVETICA, 16, Font.BOLD, ACCENT));
        logo.add(new Paragraph("Practice Manager", new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY)));
        leftCell.addElement(logo);
        headerTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph titleP = new Paragraph(reportTitle, titleFont);
        titleP.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(titleP);
        rightCell.addElement(new Paragraph("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), smallFont));
        headerTable.addCell(rightCell);

        doc.add(headerTable);

        Paragraph line = new Paragraph();
        line.add(new LineSeparator(1f, 100, PRIMARY, Element.ALIGN_CENTER, -2));
        doc.add(line);

        // Advocate creds
        PdfPTable advTable = new PdfPTable(2);
        advTable.setWidthPercentage(100);
        advTable.setSpacingAfter(15);

        PdfPCell c1 = new PdfPCell(new Paragraph(
                "Advocate: " + advocate.getFullName() + "\n" +
                "Bar Council: " + (advocate.getBarCouncilId() != null ? advocate.getBarCouncilId() : "N/A") + "\n" +
                "Specialization: " + (advocate.getSpecialization() != null ? advocate.getSpecialization() : "General"),
                normalFont));
        c1.setBorder(Rectangle.NO_BORDER);

        PdfPCell c2 = new PdfPCell(new Paragraph(
                "Email: " + advocate.getEmail() + "\n" +
                "Phone: " + (advocate.getPhone() != null ? advocate.getPhone() : "N/A") + "\n" +
                "Address: " + (advocate.getAddress() != null ? advocate.getAddress() : "N/A"),
                normalFont));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        advTable.addCell(c1);
        advTable.addCell(c2);
        doc.add(advTable);
    }

    private void addFooter(Document doc, PdfWriter writer, int pageNumber) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        cb.beginText();
        try {
            cb.setFontAndSize(BaseFont.createFont("Helvetica", "Cp1252", BaseFont.NOT_EMBEDDED), 7);
        } catch (Exception e) {
            try { cb.setFontAndSize(BaseFont.createFont("Helvetica", "WinAnsi", BaseFont.NOT_EMBEDDED), 7); } catch (Exception ex) { /* ignore */ }
        }
        cb.setColorFill(GRAY);
        String text = "Page " + pageNumber + " | Advocate App Practice Manager | Confidential";
        float textWidth = cb.getEffectiveStringWidth(text, false);
        cb.setTextMatrix((PageSize.A4.getWidth() - textWidth) / 2, 20);
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    public void addWatermark(PdfWriter writer) {
        PdfContentByte cb = writer.getDirectContentUnder();
        cb.saveState();
        cb.beginText();
        try {
            cb.setFontAndSize(BaseFont.createFont("Helvetica", "Cp1252", BaseFont.NOT_EMBEDDED), 48);
        } catch (Exception e) {
            try { cb.setFontAndSize(BaseFont.createFont("Helvetica", "WinAnsi", BaseFont.NOT_EMBEDDED), 48); } catch (Exception ex) { /* ignore */ }
        }
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.04f);
        cb.setGState(gs);
        cb.setColorFill(Color.BLACK);
        cb.showTextAligned(Element.ALIGN_CENTER, "ADVOCATE APP",
                PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2, 45);
        cb.endText();
        cb.restoreState();
    }

    private PdfPTable styledTable(float[] widths, String[] headers) {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        table.setSpacingBefore(8);
        table.setSpacingAfter(8);
        for (int i = 0; i < headers.length; i++) {
            PdfPCell cell = new PdfPCell(new Paragraph(headers[i], headerFont));
            cell.setBackgroundColor(PRIMARY);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        return table;
    }

    private void addCell(PdfPTable table, String text, boolean bold, boolean right) {
        Font f = bold ? boldFont : normalFont;
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "N/A", f));
        cell.setPadding(4);
        if (right) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    // ───────────── Existing Methods (preserved) ─────────────

    public void generateCaseReport(ByteArrayOutputStream out, List<CaseEntity> cases, Advocate advocate) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        addHeader(doc, advocate, "Case Management Report");
        PdfPTable table = styledTable(new float[]{3f, 4f, 3f, 2.5f, 2.5f}, new String[]{"Case Number", "Title", "Type", "Status", "Client"});
        for (CaseEntity c : cases) {
            addCell(table, c.getCaseNumber(), false, false);
            addCell(table, c.getCaseTitle(), false, false);
            addCell(table, c.getCaseType(), false, false);
            addCell(table, c.getStatus(), false, false);
            addCell(table, c.getClient() != null ? c.getClient().getName() : "N/A", false, false);
        }
        doc.add(table);
        doc.close();
    }

    public void generateClientReport(ByteArrayOutputStream out, List<Client> clients, Advocate advocate) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        addHeader(doc, advocate, "Client Directory Report");
        PdfPTable table = styledTable(new float[]{4f, 4f, 3f, 4f}, new String[]{"Name", "Email", "Phone", "Address"});
        for (Client c : clients) {
            addCell(table, c.getName(), false, false);
            addCell(table, c.getEmail(), false, false);
            addCell(table, c.getPhone(), false, false);
            addCell(table, c.getAddress() != null ? c.getAddress() : "N/A", false, false);
        }
        doc.add(table);
        doc.close();
    }

    public void generateExpenseReport(ByteArrayOutputStream out, List<Expense> expenses, Advocate advocate) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        addHeader(doc, advocate, "Expense Statement");
        PdfPTable table = styledTable(new float[]{3f, 2.5f, 2.5f, 2f, 2.5f}, new String[]{"Title", "Category", "Amount", "Status", "Date"});
        double total = 0;
        for (Expense e : expenses) {
            double amt = e.getAmount() != null ? e.getAmount() : 0;
            total += amt;
            addCell(table, e.getTitle(), false, false);
            addCell(table, e.getCategory(), false, false);
            addCell(table, String.format("Rs. %,.2f", amt), false, true);
            addCell(table, e.getPaymentStatus(), false, false);
            addCell(table, e.getPaymentDate() != null ? e.getPaymentDate().toString() : "N/A", false, false);
        }
        doc.add(table);
        Paragraph summary = new Paragraph(String.format("Total Expenses: Rs. %,.2f", total), subTitleFont);
        summary.setAlignment(Element.ALIGN_RIGHT);
        doc.add(summary);
        doc.close();
    }

    public void generateInvoicePdf(ByteArrayOutputStream out, Invoice invoice) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        addHeader(doc, invoice.getAdvocate(), "INVOICE: " + invoice.getInvoiceNumber());
        doc.add(new Paragraph(" "));

        PdfPTable billTable = new PdfPTable(2);
        billTable.setWidthPercentage(100);
        billTable.setSpacingAfter(15);
        PdfPCell bc1 = new PdfPCell(new Paragraph("Bill To:\n" + invoice.getClient().getName() +
                "\nEmail: " + invoice.getClient().getEmail() + "\nPhone: " + invoice.getClient().getPhone(), normalFont));
        bc1.setBorder(Rectangle.NO_BORDER);
        PdfPCell bc2 = new PdfPCell(new Paragraph("Invoice Date: " + invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) +
                "\nDue Date: " + invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) +
                "\nStatus: " + invoice.getStatus(), normalFont));
        bc2.setBorder(Rectangle.NO_BORDER);
        bc2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        billTable.addCell(bc1);
        billTable.addCell(bc2);
        doc.add(billTable);

        Paragraph ci = new Paragraph("Case: " + invoice.getCaseEntity().getCaseNumber() + " — " + invoice.getCaseEntity().getCaseTitle(), subTitleFont);
        ci.setSpacingAfter(10);
        doc.add(ci);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{7f, 3f});
        PdfPCell h1 = new PdfPCell(new Paragraph("Description", headerFont));
        h1.setBackgroundColor(PRIMARY); h1.setPadding(6);
        table.addCell(h1);
        PdfPCell h2 = new PdfPCell(new Paragraph("Amount", headerFont));
        h2.setBackgroundColor(PRIMARY); h2.setPadding(6); h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(h2);
        table.addCell(new PdfPCell(new Paragraph("Professional legal services as per agreement", normalFont)));
        PdfPCell amtCell = new PdfPCell(new Paragraph(String.format("Rs. %,.2f", invoice.getAmount()), boldFont));
        amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(amtCell);
        doc.add(table);

        Paragraph total = new Paragraph(String.format("Total Due: Rs. %,.2f", invoice.getAmount()), new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY));
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        doc.add(total);

        // QR Code Placeholder
        Paragraph qrPlaceholder = new Paragraph("[ QR Code Placeholder — Digital Verification ]", smallFont);
        qrPlaceholder.setAlignment(Element.ALIGN_CENTER);
        qrPlaceholder.setSpacingBefore(20);
        doc.add(qrPlaceholder);

        Paragraph signature = new Paragraph("Authorized Signature: ___________________", normalFont);
        signature.setSpacingBefore(30);
        doc.add(signature);

        int pageNum = writer.getPageNumber();
        addFooter(doc, writer, pageNum);
        doc.close();
    }

    public void generateReceiptPdf(ByteArrayOutputStream out, ClientPayment payment) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        addHeader(doc, payment.getAdvocate(), "PAYMENT RECEIPT");
        doc.add(new Paragraph(" "));

        PdfPTable billTable = new PdfPTable(2);
        billTable.setWidthPercentage(100);
        billTable.setSpacingAfter(15);
        PdfPCell bc1 = new PdfPCell(new Paragraph("Received From:\n" + payment.getClient().getName() +
                "\nEmail: " + payment.getClient().getEmail() + "\nPhone: " + payment.getClient().getPhone(), normalFont));
        bc1.setBorder(Rectangle.NO_BORDER);
        PdfPCell bc2 = new PdfPCell(new Paragraph("Receipt No: REC-" + payment.getId() +
                "\nPayment Date: " + (payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "N/A") +
                "\nRef No: " + (payment.getReferenceNumber() != null ? payment.getReferenceNumber() : "N/A"), normalFont));
        bc2.setBorder(Rectangle.NO_BORDER);
        bc2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        billTable.addCell(bc1);
        billTable.addCell(bc2);
        doc.add(billTable);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{7f, 3f});
        PdfPCell h1 = new PdfPCell(new Paragraph("Description", headerFont));
        h1.setBackgroundColor(PRIMARY); h1.setPadding(6);
        table.addCell(h1);
        PdfPCell h2 = new PdfPCell(new Paragraph("Amount", headerFont));
        h2.setBackgroundColor(PRIMARY); h2.setPadding(6); h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(h2);
        String desc = "Legal fee — Case " + (payment.getCaseEntity() != null ? payment.getCaseEntity().getCaseNumber() : "N/A");
        if (payment.getPaymentMode() != null) desc += " | Mode: " + payment.getPaymentMode();
        table.addCell(new PdfPCell(new Paragraph(desc, normalFont)));
        PdfPCell amtCell = new PdfPCell(new Paragraph(String.format("Rs. %,.2f", payment.getAmount()), boldFont));
        amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(amtCell);
        doc.add(table);

        Paragraph total = new Paragraph(String.format("Amount Paid: Rs. %,.2f", payment.getAmount()), new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY));
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        doc.add(total);

        Paragraph sig = new Paragraph("Authorized Signature: ___________________", normalFont);
        sig.setSpacingBefore(30);
        doc.add(sig);

        addFooter(doc, writer, writer.getPageNumber());
        doc.close();
    }

    // ───────────── New Templates ─────────────

    public void generateClientDetailReport(ByteArrayOutputStream out, ClientDetailReportDTO dto, Advocate advocate) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        addHeader(doc, advocate, "Client Report: " + dto.getName());
        addInfoRow(doc, "Client Details", new String[][]{
            {"Name", dto.getName()}, {"Phone", dto.getPhone()}, {"Email", dto.getEmail()},
            {"Address", dto.getAddress()}, {"Registered", dto.getRegistrationDate()}
        });
        addInfoRow(doc, "Case Summary", new String[][]{
            {"Total Cases", String.valueOf(dto.getTotalCases())},
            {"Active", String.valueOf(dto.getActiveCases())},
            {"Closed", String.valueOf(dto.getClosedCases())},
            {"Pending", String.valueOf(dto.getPendingCases())}
        });
        if (dto.getDocuments() != null && !dto.getDocuments().isEmpty()) {
            doc.add(new Paragraph("Documents (" + dto.getDocuments().size() + ")", subTitleFont));
            for (String docName : dto.getDocuments()) {
                doc.add(new Paragraph("  • " + docName, normalFont));
            }
        }
        if (dto.getRecentPayments() != null && !dto.getRecentPayments().isEmpty()) {
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Recent Payments", subTitleFont));
            PdfPTable pt = styledTable(new float[]{3f, 2.5f, 2f, 2.5f}, new String[]{"Date", "Amount", "Mode", "Reference"});
            for (ClientDetailReportDTO.PaymentEntry pe : dto.getRecentPayments()) {
                addCell(pt, pe.getDate(), false, false);
                addCell(pt, String.format("Rs. %,.2f", pe.getAmount()), false, true);
                addCell(pt, pe.getMode(), false, false);
                addCell(pt, pe.getReference(), false, false);
            }
            doc.add(pt);
        }
        Paragraph qr = new Paragraph("[ QR Code — Verification Placeholder ]", smallFont);
        qr.setAlignment(Element.ALIGN_CENTER);
        qr.setSpacingBefore(20);
        doc.add(qr);
        addFooter(doc, writer, writer.getPageNumber());
        doc.close();
    }

    public void generateCaseDetailReport(ByteArrayOutputStream out, CaseDetailReportDTO dto, Advocate advocate) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        addHeader(doc, advocate, "Case Report: " + dto.getCaseNumber());
        addInfoRow(doc, "Case Information", new String[][]{
            {"Case Number", dto.getCaseNumber()}, {"Title", dto.getCaseTitle()},
            {"Type", dto.getCaseType()}, {"Court", dto.getCourtLevel()},
            {"Client", dto.getClientName()}, {"Status", dto.getStatus()},
            {"Filed Date", dto.getFiledDate()}, {"Next Hearing", dto.getNextHearing()},
            {"Advocate", dto.getAdvocateName()}
        });
        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            doc.add(new Paragraph("Description", subTitleFont));
            doc.add(new Paragraph(dto.getDescription(), normalFont));
            doc.add(new Paragraph(" "));
        }
        if (dto.getInvoices() != null && !dto.getInvoices().isEmpty()) {
            doc.add(new Paragraph("Invoices", subTitleFont));
            PdfPTable it = styledTable(new float[]{4f, 3f, 3f}, new String[]{"Invoice Number", "Amount", "Status"});
            for (CaseDetailReportDTO.InvoiceEntry ie : dto.getInvoices()) {
                addCell(it, ie.getNumber(), false, false);
                addCell(it, String.format("Rs. %,.2f", ie.getAmount()), false, true);
                addCell(it, ie.getStatus(), false, false);
            }
            doc.add(it);
        }
        doc.add(new Paragraph(String.format("Total Expenses: Rs. %,.2f", dto.getTotalExpenses()), boldFont));
        doc.add(new Paragraph(String.format("Total Payments: Rs. %,.2f", dto.getTotalPayments()), boldFont));

        if (dto.getDocuments() != null && !dto.getDocuments().isEmpty()) {
            doc.add(new Paragraph("Documents (" + dto.getDocuments().size() + ")", subTitleFont));
            for (String dn : dto.getDocuments()) doc.add(new Paragraph("  • " + dn, normalFont));
        }
        if (dto.getTimeline() != null && !dto.getTimeline().isEmpty()) {
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Case Timeline", subTitleFont));
            PdfPTable tt = styledTable(new float[]{3f, 7f}, new String[]{"Date", "Event"});
            for (CaseDetailReportDTO.TimelineEntry te : dto.getTimeline()) {
                addCell(tt, te.getDate(), false, false);
                addCell(tt, te.getEvent(), false, false);
            }
            doc.add(tt);
        }
        Paragraph qr = new Paragraph("[ QR Code — Verification Placeholder ]", smallFont);
        qr.setAlignment(Element.ALIGN_CENTER);
        qr.setSpacingBefore(15);
        doc.add(qr);
        addFooter(doc, writer, writer.getPageNumber());
        doc.close();
    }

    public void generateMonthlyReport(ByteArrayOutputStream out, Advocate advocate, int year, int month,
                                       long totalClients, long newClients, long totalCases, long active, long closed,
                                       long pending, long dismissed, double income, double expense, double profit,
                                       long hearings, long invoicesGenerated, long paymentsReceived) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        String monthName = LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        addHeader(doc, advocate, "Monthly Report — " + monthName);

        addInfoRow(doc, "Client Summary", new String[][]{
            {"Total Clients", String.valueOf(totalClients)}, {"New Clients", String.valueOf(newClients)}
        });
        addInfoRow(doc, "Case Summary", new String[][]{
            {"Total Cases", String.valueOf(totalCases)}, {"Active", String.valueOf(active)},
            {"Closed", String.valueOf(closed)}, {"Pending", String.valueOf(pending)},
            {"Dismissed", String.valueOf(dismissed)}
        });
        addInfoRow(doc, "Financial Summary", new String[][]{
            {"Income", String.format("Rs. %,.2f", income)},
            {"Expenses", String.format("Rs. %,.2f", expense)},
            {"Profit", String.format("Rs. %,.2f", profit)}
        });
        addInfoRow(doc, "Activity", new String[][]{
            {"Hearings", String.valueOf(hearings)}, {"Invoices", String.valueOf(invoicesGenerated)},
            {"Payments", String.valueOf(paymentsReceived)}
        });

        Paragraph qr = new Paragraph("[ QR Code — Verification Placeholder ]", smallFont);
        qr.setAlignment(Element.ALIGN_CENTER);
        qr.setSpacingBefore(20);
        doc.add(qr);
        addFooter(doc, writer, writer.getPageNumber());
        doc.close();
    }

    public void generateDashboardReport(ByteArrayOutputStream out, Advocate advocate,
                                         Map<String, Object> stats) throws DocumentException {
        Document doc = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();
        addWatermark(writer);
        addHeader(doc, advocate, "Dashboard Report");

        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) stats.get("summary");
        if (summary != null) {
            addInfoRow(doc, "Overview", new String[][]{
                {"Total Cases", String.valueOf(summary.getOrDefault("totalCases", 0))},
                {"Active Cases", String.valueOf(summary.getOrDefault("activeCases", 0))},
                {"Total Clients", String.valueOf(summary.getOrDefault("totalClients", 0))},
                {"Upcoming Hearings", String.valueOf(summary.getOrDefault("upcomingHearings", 0))},
                {"Pending Invoices", String.valueOf(summary.getOrDefault("pendingInvoices", 0))}
            });
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> financial = (Map<String, Object>) stats.get("financial");
        if (financial != null) {
            addInfoRow(doc, "Financial Summary", new String[][]{
                {"Income", String.format("Rs. %,.2f", financial.getOrDefault("income", 0))},
                {"Expenses", String.format("Rs. %,.2f", financial.getOrDefault("expenses", 0))},
                {"Total Documents", String.valueOf(stats.getOrDefault("totalDocuments", 0))}
            });
        }

        @SuppressWarnings("unchecked")
        Map<String, Long> caseStatus = (Map<String, Long>) stats.get("caseStatus");
        if (caseStatus != null) {
            String[][] data = caseStatus.entrySet().stream()
                .map(e -> new String[]{e.getKey(), String.valueOf(e.getValue())})
                .toArray(String[][]::new);
            addInfoRow(doc, "Case Status Distribution", data);
        }

        Paragraph qr = new Paragraph("[ QR Code — Verification Placeholder ]", smallFont);
        qr.setAlignment(Element.ALIGN_CENTER);
        qr.setSpacingBefore(20);
        doc.add(qr);
        addFooter(doc, writer, writer.getPageNumber());
        doc.close();
    }

    private void addInfoRow(Document doc, String sectionTitle, String[][] rows) throws DocumentException {
        doc.add(new Paragraph(sectionTitle, subTitleFont));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 5f});
        table.setSpacingAfter(10);
        boolean alt = false;
        for (String[] row : rows) {
            PdfPCell kc = new PdfPCell(new Paragraph(row[0], boldFont));
            kc.setPadding(4);
            if (alt) kc.setBackgroundColor(ROW_ALT);
            table.addCell(kc);
            PdfPCell vc = new PdfPCell(new Paragraph(row.length > 1 ? row[1] : "", normalFont));
            vc.setPadding(4);
            if (alt) vc.setBackgroundColor(ROW_ALT);
            table.addCell(vc);
            alt = !alt;
        }
        doc.add(table);
    }


}
