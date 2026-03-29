package com.payflow.ms_notification.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferEvent(
        UUID transactionId,
        UUID senderId,
        UUID receiverId,
        BigDecimal value,
        String description
) {}