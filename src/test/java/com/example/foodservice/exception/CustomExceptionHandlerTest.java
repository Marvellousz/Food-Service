package com.example.foodservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomExceptionHandlerTest {

    @InjectMocks
    private CustomExceptionHandler customExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Test
    void handleFoodNotFoundException_shouldReturnNotFoundStatus() {
        // Given
        String errorMessage = "Food item not found with id: 99";
        FoodNotFoundException ex = new FoodNotFoundException(errorMessage);
        String requestURI = "/api/foods/99";

        when(request.getRequestURI()).thenReturn(requestURI);

        // When
        ResponseEntity<ErrorResponse> responseEntity = customExceptionHandler.handleFoodNotFoundException(ex, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(requestURI, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleGlobalException_shouldReturnInternalServerErrorStatus() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");
        String requestURI = "/api/foods";

        when(request.getRequestURI()).thenReturn(requestURI);

        // When
        ResponseEntity<ErrorResponse> responseEntity = customExceptionHandler.handleGlobalException(ex, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), errorResponse.getError());
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
        assertEquals(requestURI, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }
}
