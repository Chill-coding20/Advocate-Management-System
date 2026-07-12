package advocate.com.advocate_app.communication.provider;

import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.enums.NotificationChannel;

public interface NotificationProvider {

    boolean supports(NotificationChannel channel);

    NotificationResult send(NotificationPayload payload);

    class NotificationResult {
        private final boolean success;
        private final String providerResponse;
        private final String errorMessage;

        public NotificationResult(boolean success, String providerResponse, String errorMessage) {
            this.success = success;
            this.providerResponse = providerResponse;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getProviderResponse() { return providerResponse; }
        public String getErrorMessage() { return errorMessage; }
    }
}
