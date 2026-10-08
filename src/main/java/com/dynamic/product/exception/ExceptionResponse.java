package com.dynamic.product.exception;

import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class ExceptionResponse {

    private int status;
    private String message;
    private LocalDateTime time;

    public ExceptionResponse(){

    }

    public ExceptionResponse(int status, String message, LocalDateTime time) {
        this.status = status;
        this.message = message;
        this.time = time;
    }
}
