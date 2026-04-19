package com.microservices.ecommerce.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> UserNotFoundException(UserNotFoundException ex){
        String message = ex.getMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse(message,false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException ex){
        String message = ex.getMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse(message,false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException ex){
        String message = ex.getMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse(message,false);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.FORBIDDEN);
    }
}
