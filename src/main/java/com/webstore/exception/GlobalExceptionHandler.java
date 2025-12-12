package com.webstore.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        LOGGER.error("Webstore: EntityNotFoundException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Webstore: The requested resource was not found.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOGGER.error("Webstore: IllegalArgumentException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Webstore: Invalid input provided. Please check your request.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        LOGGER.error("Webstore: ResponseStatusException occurred: {}", ex.getMessage(), ex);
        String reason = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode())
                .body("Webstore: " + reason);
    }

    // ✅ Custom WhatsApp flow-related exceptions
    @ExceptionHandler(WhatsAppFlowException.class)
    public ResponseEntity<String> handleWhatsAppFlowException(WhatsAppFlowException ex) {
        LOGGER.warn("Webstore: WhatsApp flow exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Webstore: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        LOGGER.error("Webstore: Unexpected exception occurred: {}", ex.getMessage(), ex);
        LOGGER.error("Exception type: {}", ex.getClass().getName());
        LOGGER.error("Full stack trace:", ex);
        
        // Return detailed error message for debugging
        String errorDetails = String.format(
            "Webstore Error:\n" +
            "Type: %s\n" +
            "Message: %s\n" +
            "Check application logs for full stack trace.",
            ex.getClass().getSimpleName(),
            ex.getMessage() != null ? ex.getMessage() : "No error message available"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDetails);
    }
}
