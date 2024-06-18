package nl.abcbank.apigateway.controller;

import jakarta.annotation.Nonnull;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nl.abcbank.apigateway.exception.ServiceException;
import nl.abcbank.openapi.apigateway.external.model.ErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR = "Internal server error";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception e) {
        log.error("", e);

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> handleServiceException(ServiceException e) {
        if (e.getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error(e.getMessage(), e.getCause());
        } else {
            log.info(e.getMessage(), e.getCause());
        }

        if (StringUtils.isEmpty(e.getMessage())) {
            return ResponseEntity.status(e.getStatus())
                    .body(null);
        }

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(e.getMessage());

        return ResponseEntity.status(e.getStatus())
                .body(errorMessage);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@Nonnull MethodArgumentNotValidException ex,
                                                                  @Nonnull HttpHeaders headers,
                                                                  @Nonnull HttpStatusCode status,
                                                                  @Nonnull WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(d -> String.format("%s: %s", d.getField(), d.getDefaultMessage()))
                .sorted()
                .collect(Collectors.joining(", "));

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(message);

        return handleExceptionInternal(ex, errorMessage, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@Nonnull HttpMessageNotReadableException ex,
                                                                  @Nonnull HttpHeaders headers,
                                                                  @Nonnull HttpStatusCode status,
                                                                  @Nonnull WebRequest request) {
        log.error(ex.getMessage(), ex.getCause());

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(ex.getMessage());

        return handleExceptionInternal(ex, errorMessage, headers, HttpStatus.BAD_REQUEST, request);
    }

}