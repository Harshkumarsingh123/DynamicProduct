package com.dynamic.product.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponse> handleApplicationException(
            CustomException exception) {

        ExceptionResponse response = new ExceptionResponse(
                exception.getStatus().value(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(
                response,
                exception.getStatus()
        );
    }
}