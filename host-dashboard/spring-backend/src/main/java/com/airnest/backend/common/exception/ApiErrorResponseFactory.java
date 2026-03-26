package com.airnest.backend.common.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorResponseFactory {

    public ApiErrorResponse create(HttpStatus status, String code, String message, String path) {
        return create(status, code, message, path, Collections.emptyMap());
    }

    public ApiErrorResponse create(
        HttpStatus status,
        String code,
        String message,
        String path,
        Map<String, String> details
    ) {
        return new ApiErrorResponse(
            Instant.now(),
            status.value(),
            message,
            message,
            code,
            path,
            details == null || details.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(details))
        );
    }
}
