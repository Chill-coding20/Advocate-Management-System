package advocate.com.advocate_app.communication.exception;

public class MetaWhatsAppException extends RuntimeException {

    private final Integer statusCode;
    private final String responseBody;

    public MetaWhatsAppException(Integer statusCode, String message, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public MetaWhatsAppException(Integer statusCode, String message, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
