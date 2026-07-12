package advocate.com.advocate_app.dto;

public class AssistantRequest {
    private String query;
    private String currentRoute;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getCurrentRoute() { return currentRoute; }
    public void setCurrentRoute(String currentRoute) { this.currentRoute = currentRoute; }
}
