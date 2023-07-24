package com.emp.management.system.exception;



import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<Exception> handleRequestException(RequestException e) {
        Exception exception = new Exception(
                e.getMessage(),
                e.getCause(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Exception> handleIllegalArgumentException(IllegalArgumentException e) {
        Exception exception = new Exception(
                e.getMessage(),
                e.getCause(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Exception> handleIllegalStateException(IllegalStateException e) {
        Exception exception = new Exception(
                e.getMessage(),
                e.getCause(),
                HttpStatus.CONFLICT,
                ZonedDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Exception> handleThrowable(Throwable t) {
        Exception exception = new Exception(
                t.getMessage(),
                t.getCause(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception);
    }
}
