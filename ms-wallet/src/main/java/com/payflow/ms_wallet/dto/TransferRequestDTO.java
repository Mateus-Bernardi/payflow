package com.payflow.ms_wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDTO(
        @NotNull UUID receiverId,

        @NotNull
        @DecimalMin(value = "0.01", message = "O valor mínimo é 1 centavo")
        BigDecimal value
) {}