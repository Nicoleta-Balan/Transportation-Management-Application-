package multitier.trans.exception;

import multitier.trans.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ResourceNotFoundException (404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        );
        error.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // ValidationException (400 Bad Request)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, WebRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        error.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    // BusinessException (400 Bad Request)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        logger.warn("Business logic error: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        error.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    // Bean Validation errors (400 Bad Request)
    // Catches errors from @Valid annotations on request bodies
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Method argument validation error: {}", ex.getMessage());
        
        StringBuilder messageBuilder = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            messageBuilder.append(error.getField())
                         .append(" ")
                         .append(error.getDefaultMessage())
                         .append("; ")
        );
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            messageBuilder.toString()
        );
        error.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    // Constraint Validation errors (400 Bad Request)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation error: " + ex.getMessage()
        );
        error.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    // IllegalArgumentException (400 Bad Request)
    // Catch-all for argument validation errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        error.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    // Database constraint violations (400 Bad Request).
    // Catches unique constraint violations and foreign key violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        logger.warn("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity error: ";
        if (ex.getMessage() != null && ex.getMessage().contains("unique constraint")) {
            message += "A record with this combination already exists.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("foreign key")) {
            message += "Cannot perform this operation due to related records.";
        } else {
            message += ex.getMostSpecificCause() != null 
                ? ex.getMostSpecificCause().getMessage() 
                : "Database constraint violation.";
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message
        );
        error.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Unhandled exceptions (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please contact support if the problem persists.",
            ex.getMessage()
        );
        error.setPath(request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

