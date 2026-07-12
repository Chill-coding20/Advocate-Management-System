package advocate.com.advocate_app.dto;

public class PreferencesRequestDTO {

    private String theme;
    private String language;
    private String timeZone;
    private String currency;
    private String dateFormat;
    private Integer autoLogoutDuration;
    private String defaultDashboardFilter;

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public Integer getAutoLogoutDuration() { return autoLogoutDuration; }
    public void setAutoLogoutDuration(Integer autoLogoutDuration) { this.autoLogoutDuration = autoLogoutDuration; }

    public String getDefaultDashboardFilter() { return defaultDashboardFilter; }
    public void setDefaultDashboardFilter(String defaultDashboardFilter) { this.defaultDashboardFilter = defaultDashboardFilter; }
}
