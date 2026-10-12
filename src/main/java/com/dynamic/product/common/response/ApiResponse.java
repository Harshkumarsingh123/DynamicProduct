
package com.dynamic.product.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private int status;
    private String message;
    private LocalDateTime time;
    private T data;

    public static <T> ApiResponse<T> success(
            int status, String message, T data) {

        return new ApiResponse<>(
                status,
                message,
                LocalDateTime.now(),
                data
        );
    }
}
