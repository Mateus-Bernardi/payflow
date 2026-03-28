package com.payflow.ms_ai_categorizer.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferEvent(
        UUID senderId,
        UUID receiverId,
        BigDecimal value,
        String description
) {}