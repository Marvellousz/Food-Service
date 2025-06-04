package com.example.foodservice.exception;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseTest {

    @Test
    void testErrorResponseConstructorAndGetters() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/foods/999";

        // When
        ErrorResponse errorResponse = new ErrorResponse(timestamp, status, error, message, path);

        // Then
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    @Test
    void testErrorResponseNoArgsConstructorAndSetters() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 500;
        String error = "Internal Server Error";
        String message = "An unexpected error occurred";
        String path = "/api/foods";

        // When
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(timestamp);
        errorResponse.setStatus(status);
        errorResponse.setError(error);
        errorResponse.setMessage(message);
        errorResponse.setPath(path);

        // Then
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    @Test
    void testErrorResponseEqualsAndHashCode() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse errorResponse1 = new ErrorResponse(timestamp, 404, "Not Found", "Resource not found", "/api/foods/999");
        ErrorResponse errorResponse2 = new ErrorResponse(timestamp, 404, "Not Found", "Resource not found", "/api/foods/999");
        ErrorResponse differentErrorResponse = new ErrorResponse(timestamp, 500, "Server Error", "Internal error", "/api/foods");

        // Then
        assertEquals(errorResponse1, errorResponse2);
        assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode());
        assertNotEquals(errorResponse1, differentErrorResponse);
        assertNotEquals(errorResponse1.hashCode(), differentErrorResponse.hashCode());
    }

    @Test
    void testErrorResponseToString() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse errorResponse = new ErrorResponse(timestamp, 404, "Not Found", "Resource not found", "/api/foods/999");

        // When
        String toStringResult = errorResponse.toString();

        // Then
        assertTrue(toStringResult.contains("ErrorResponse"));
        assertTrue(toStringResult.contains("timestamp=" + timestamp));
        assertTrue(toStringResult.contains("status=404"));
        assertTrue(toStringResult.contains("error=Not Found"));
        assertTrue(toStringResult.contains("message=Resource not found"));
        assertTrue(toStringResult.contains("path=/api/foods/999"));
    }
}
