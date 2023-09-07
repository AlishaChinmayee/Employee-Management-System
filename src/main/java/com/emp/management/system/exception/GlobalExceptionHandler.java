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
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Exception> handleRuntimeException(RuntimeException e) {
        Exception exception = new Exception(
                "Failed to create employee account due to an internal server error", // Customize the error message
                e, // Pass the original exception as the cause
                HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception);
    }
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<String> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
