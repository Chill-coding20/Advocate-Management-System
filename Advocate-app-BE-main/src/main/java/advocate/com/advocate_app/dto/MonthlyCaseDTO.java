package advocate.com.advocate_app.dto;

import java.util.List;

public class MonthlyCaseDTO {
    private List<MonthItem> items;

    public MonthlyCaseDTO() {}

    public MonthlyCaseDTO(List<MonthItem> items) {
        this.items = items;
    }

    public List<MonthItem> getItems() { return items; }
    public void setItems(List<MonthItem> items) { this.items = items; }

    public static class MonthItem {
        private String month;
        private long created;
        private long closed;
        private long pending;
        private long dismissed;

        public MonthItem() {}

        public MonthItem(String month, long created, long closed, long pending, long dismissed) {
            this.month = month;
            this.created = created;
            this.closed = closed;
            this.pending = pending;
            this.dismissed = dismissed;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public long getCreated() { return created; }
        public void setCreated(long created) { this.created = created; }

        public long getClosed() { return closed; }
        public void setClosed(long closed) { this.closed = closed; }

        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }

        public long getDismissed() { return dismissed; }
        public void setDismissed(long dismissed) { this.dismissed = dismissed; }
    }
}
