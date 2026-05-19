package com.microservices.ecommerce.order.exception;

import com.microservices.ecommerce.order.dto.OrderResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartServiceUnavailableException.class)
    public ResponseEntity<OrderResponseDTO> handleCartServiceUnavailable(CartServiceUnavailableException exception) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setMessage(exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
