package advocate.com.advocate_app.dto;

import java.util.List;

public class CourtStatsDTO {
    private List<CourtItem> items;

    public CourtStatsDTO() {}

    public CourtStatsDTO(List<CourtItem> items) {
        this.items = items;
    }

    public List<CourtItem> getItems() { return items; }
    public void setItems(List<CourtItem> items) { this.items = items; }

    public static class CourtItem {
        private String court;
        private long active;
        private long pending;
        private long closed;
        private long dismissed;

        public CourtItem() {}

        public CourtItem(String court, long active, long pending, long closed, long dismissed) {
            this.court = court;
            this.active = active;
            this.pending = pending;
            this.closed = closed;
            this.dismissed = dismissed;
        }

        public String getCourt() { return court; }
        public void setCourt(String court) { this.court = court; }

        public long getActive() { return active; }
        public void setActive(long active) { this.active = active; }

        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }

        public long getClosed() { return closed; }
        public void setClosed(long closed) { this.closed = closed; }

        public long getDismissed() { return dismissed; }
        public void setDismissed(long dismissed) { this.dismissed = dismissed; }
    }
}
