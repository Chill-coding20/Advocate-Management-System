package advocate.com.advocate_app.exception;

import advocate.com.advocate_app.security.PermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> errorBody(String message, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("status", status);
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(ex.getMessage(), 404));
    }

    @ExceptionHandler(DuplicateCaseNumberException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCase(DuplicateCaseNumberException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(ex.getMessage(), 409));
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<Map<String, Object>> handlePermissionDenied(PermissionDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody(ex.getMessage(), 403));
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFileValidation(FileValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ex.getMessage(), 400));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(ex.getMessage(), 401));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("Invalid request: " + ex.getMessage(), 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("An unexpected error occurred", 500));
    }
}
