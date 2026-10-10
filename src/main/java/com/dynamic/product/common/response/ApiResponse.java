package com.dynamic.product.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiResponse {

    private int status;
    private String message;
    private LocalDateTime time;
}