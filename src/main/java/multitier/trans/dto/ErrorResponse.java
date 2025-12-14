package multitier.trans.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
public class ErrorResponse {
    
    private int status;
    private String message;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(int status, String message) {
        this();
        this.status = status;
        this.message = message;
        this.error = message;
    }
    
    public ErrorResponse(int status, String message, String error) {
        this();
        this.status = status;
        this.message = message;
        this.error = error;
    }
}

