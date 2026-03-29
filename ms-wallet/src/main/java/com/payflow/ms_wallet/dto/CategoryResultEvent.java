package com.payflow.ms_wallet.dto;

import java.util.UUID;

public record CategoryResultEvent(
        UUID transactionId,
        String category
) {}
