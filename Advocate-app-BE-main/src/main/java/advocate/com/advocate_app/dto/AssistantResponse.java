package advocate.com.advocate_app.dto;

import java.util.List;
import java.util.Map;

public class AssistantResponse {
    private String intent;              // e.g. SHOW_SUMMARY, OPEN_PAGE, SEARCH_CLIENT
    private String action;              // OPEN_PAGE, SEARCH, OPEN_MODAL, SHOW_DATA, ANSWER
    private String route;               // React route to navigate to
    private String message;             // Human-readable text reply
    private String searchQuery;         // Auto-fill search if applicable
    private String modalToOpen;         // e.g. "create-client", "create-case"
    private Map<String, Object> data;   // Structured data for the assistant to display
    private List<Map<String, Object>> results; // Search results
    private String highlightId;         // ID of record to highlight

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public String getModalToOpen() { return modalToOpen; }
    public void setModalToOpen(String modalToOpen) { this.modalToOpen = modalToOpen; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public List<Map<String, Object>> getResults() { return results; }
    public void setResults(List<Map<String, Object>> results) { this.results = results; }

    public String getHighlightId() { return highlightId; }
    public void setHighlightId(String highlightId) { this.highlightId = highlightId; }
}
