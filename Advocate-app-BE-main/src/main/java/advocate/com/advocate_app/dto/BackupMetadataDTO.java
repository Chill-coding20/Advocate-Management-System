package advocate.com.advocate_app.dto;

import java.util.ArrayList;
import java.util.List;

public class BackupMetadataDTO {
    private String applicationVersion;
    private String backupVersion;
    private String backupDate;
    private String backupType;
    private String advocateName;
    private String advocateEmail;
    private String databaseType;
    private String databaseVersion;
    private long numberOfClients;
    private long numberOfCases;
    private long numberOfDocuments;
    private long numberOfExpenses;
    private long numberOfInvoices;
    private long numberOfNotifications;
    private long numberOfTasks;
    private long numberOfCaseEvents;
    private long numberOfActivities;
    private long zipSize;
    private Long durationSeconds;
    private String checksum;
    private List<SectionResult> sections;
    private int healthScore;

    public static class SectionResult {
        private String name;
        private String status;
        private long durationMs;
        private String error;

        public SectionResult() {}

        public SectionResult(String name, String status, long durationMs, String error) {
            this.name = name;
            this.status = status;
            this.durationMs = durationMs;
            this.error = error;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static int computeHealthScore(List<SectionResult> sections) {
        int score = 100;
        boolean dbFailed = false;
        boolean jsonFailed = false;
        for (SectionResult s : sections) {
            if (!"SUCCESS".equals(s.getStatus())) {
                switch (s.getName()) {
                    case "DATABASE": score -= 40; dbFailed = true; break;
                    case "JSON": score -= 40; jsonFailed = true; break;
                    case "DOCUMENTS": score -= 10; break;
                    case "REPORTS": score -= 5; break;
                    case "SETTINGS": score -= 5; break;
                    case "METADATA": score -= 30; break;
                }
            }
        }
        if (dbFailed && jsonFailed) score = 0;
        return Math.max(0, Math.min(100, score));
    }

    public String getApplicationVersion() { return applicationVersion; }
    public void setApplicationVersion(String applicationVersion) { this.applicationVersion = applicationVersion; }

    public String getBackupVersion() { return backupVersion; }
    public void setBackupVersion(String backupVersion) { this.backupVersion = backupVersion; }

    public String getBackupDate() { return backupDate; }
    public void setBackupDate(String backupDate) { this.backupDate = backupDate; }

    public String getBackupType() { return backupType; }
    public void setBackupType(String backupType) { this.backupType = backupType; }

    public String getAdvocateName() { return advocateName; }
    public void setAdvocateName(String advocateName) { this.advocateName = advocateName; }

    public String getAdvocateEmail() { return advocateEmail; }
    public void setAdvocateEmail(String advocateEmail) { this.advocateEmail = advocateEmail; }

    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String databaseType) { this.databaseType = databaseType; }

    public String getDatabaseVersion() { return databaseVersion; }
    public void setDatabaseVersion(String databaseVersion) { this.databaseVersion = databaseVersion; }

    public long getNumberOfClients() { return numberOfClients; }
    public void setNumberOfClients(long numberOfClients) { this.numberOfClients = numberOfClients; }

    public long getNumberOfCases() { return numberOfCases; }
    public void setNumberOfCases(long numberOfCases) { this.numberOfCases = numberOfCases; }

    public long getNumberOfDocuments() { return numberOfDocuments; }
    public void setNumberOfDocuments(long numberOfDocuments) { this.numberOfDocuments = numberOfDocuments; }

    public long getNumberOfExpenses() { return numberOfExpenses; }
    public void setNumberOfExpenses(long numberOfExpenses) { this.numberOfExpenses = numberOfExpenses; }

    public long getNumberOfInvoices() { return numberOfInvoices; }
    public void setNumberOfInvoices(long numberOfInvoices) { this.numberOfInvoices = numberOfInvoices; }

    public long getNumberOfNotifications() { return numberOfNotifications; }
    public void setNumberOfNotifications(long numberOfNotifications) { this.numberOfNotifications = numberOfNotifications; }

    public long getNumberOfTasks() { return numberOfTasks; }
    public void setNumberOfTasks(long numberOfTasks) { this.numberOfTasks = numberOfTasks; }

    public long getNumberOfCaseEvents() { return numberOfCaseEvents; }
    public void setNumberOfCaseEvents(long numberOfCaseEvents) { this.numberOfCaseEvents = numberOfCaseEvents; }

    public long getNumberOfActivities() { return numberOfActivities; }
    public void setNumberOfActivities(long numberOfActivities) { this.numberOfActivities = numberOfActivities; }

    public long getZipSize() { return zipSize; }
    public void setZipSize(long zipSize) { this.zipSize = zipSize; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public List<SectionResult> getSections() { return sections; }
    public void setSections(List<SectionResult> sections) { this.sections = sections; }

    public int getHealthScore() { return healthScore; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }
}