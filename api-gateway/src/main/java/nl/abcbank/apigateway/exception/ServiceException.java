package nl.abcbank.apigateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {

    private final Exception exception;
    private final HttpStatus status;

    public ServiceException(String message, HttpStatus status, Exception exception) {
        super(message);
        this.status = status;
        this.exception = exception;
    }

    public ServiceException(String message, HttpStatus status) {
        this(message, status, null);
    }

    public ServiceException(HttpStatus status, Exception exception) {
        this(null, status, exception);
    }

}
