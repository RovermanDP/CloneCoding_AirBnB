package com.airnest.backend.auth.security;

import com.airnest.backend.common.exception.ApiErrorResponse;
import com.airnest.backend.common.exception.ApiErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ApiErrorResponseFactory apiErrorResponseFactory;

    public RestAccessDeniedHandler(
        ObjectMapper objectMapper,
        ApiErrorResponseFactory apiErrorResponseFactory
    ) {
        this.objectMapper = objectMapper;
        this.apiErrorResponseFactory = apiErrorResponseFactory;
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        ApiErrorResponse payload = apiErrorResponseFactory.create(
            HttpStatus.FORBIDDEN,
            "access_denied",
            "Access is denied.",
            request.getRequestURI()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), payload);
    }
}
