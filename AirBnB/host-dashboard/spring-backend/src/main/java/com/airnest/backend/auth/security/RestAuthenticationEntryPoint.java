package com.airnest.backend.auth.security;

import com.airnest.backend.common.exception.ApiErrorResponse;
import com.airnest.backend.common.exception.ApiErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ApiErrorResponseFactory apiErrorResponseFactory;

    public RestAuthenticationEntryPoint(
        ObjectMapper objectMapper,
        ApiErrorResponseFactory apiErrorResponseFactory
    ) {
        this.objectMapper = objectMapper;
        this.apiErrorResponseFactory = apiErrorResponseFactory;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        ApiErrorResponse payload = apiErrorResponseFactory.create(
            HttpStatus.UNAUTHORIZED,
            "unauthorized",
            "Authentication is required.",
            request.getRequestURI()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), payload);
    }
}
