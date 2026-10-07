package com.dynamic.product.exception;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ExceptionResponse> categoryNotFoundException(CategoryNotFoundException categoryNotFoundException){

        ExceptionResponse exceptionResponse=new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(),
                categoryNotFoundException.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ExceptionResponse> productNotFoundException(ProductNotFoundException productNotFoundException){

        ExceptionResponse exceptionResponse=new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(),
                productNotFoundException.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> emailAlreadyExistsException(EmailAlreadyExistsException emailAlreadyExistsException){

        ExceptionResponse exceptionResponse=new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(),
                emailAlreadyExistsException.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionResponse,HttpStatus.BAD_REQUEST);
    }


}
