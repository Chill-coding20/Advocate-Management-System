package advocate.com.advocate_app.dto;

import java.util.List;

public class CaseStatusDTO {
    private List<StatusItem> items;

    public CaseStatusDTO() {}

    public CaseStatusDTO(List<StatusItem> items) {
        this.items = items;
    }

    public List<StatusItem> getItems() { return items; }
    public void setItems(List<StatusItem> items) { this.items = items; }

    public static class StatusItem {
        private String status;
        private long count;
        private double percentage;

        public StatusItem() {}

        public StatusItem(String status, long count, double percentage) {
            this.status = status;
            this.count = count;
            this.percentage = percentage;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
}
