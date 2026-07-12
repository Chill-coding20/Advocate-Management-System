package advocate.com.advocate_app.communication.service;

import advocate.com.advocate_app.communication.entity.NotificationHistory;
import advocate.com.advocate_app.communication.enums.NotificationStatus;
import advocate.com.advocate_app.entity.Advocate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportToCsv(List<NotificationHistory> records) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

            writer.write('\ufeff');
            writer.write("ID,Channel,Type,Recipient,Subject,Status,Sent At,Error\n");

            for (NotificationHistory h : records) {
                writer.write(escapeCsv(h.getId() != null ? h.getId().toString() : ""));
                writer.write(",");
                writer.write(escapeCsv(h.getChannel() != null ? h.getChannel().name() : ""));
                writer.write(",");
                writer.write(escapeCsv(h.getType() != null ? h.getType().name() : ""));
                writer.write(",");
                writer.write(escapeCsv(h.getRecipient()));
                writer.write(",");
                writer.write(escapeCsv(h.getSubject()));
                writer.write(",");
                writer.write(escapeCsv(h.getStatus() != null ? h.getStatus().name() : ""));
                writer.write(",");
                writer.write(escapeCsv(h.getSentAt() != null ? h.getSentAt().format(FMT) : ""));
                writer.write(",");
                writer.write(escapeCsv(h.getErrorMessage()));
                writer.write("\n");
            }

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export CSV", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
