package com.payflow.ms_auth.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponseDTO(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}