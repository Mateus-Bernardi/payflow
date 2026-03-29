package com.payflow.ms_ai_categorizer.dto;

import java.util.UUID;

public record CategoryResultEvent(
        UUID transactionId,
        String category
) {}