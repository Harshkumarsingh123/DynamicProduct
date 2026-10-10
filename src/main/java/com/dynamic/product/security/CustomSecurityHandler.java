package com.dynamic.product.security;

import com.dynamic.product.exception.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomSecurityHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {

        sendResponse(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required"
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception)
            throws IOException {

        sendResponse(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "You do not have permission to perform this action"
        );
    }

    private void sendResponse(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        ExceptionResponse error = new ExceptionResponse(
                status,
                message,
                LocalDateTime.now()
        );

        response.setStatus(status);
        response.setContentType("application/json");

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}