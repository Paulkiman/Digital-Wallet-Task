package com.task.walletservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletResponse(

        UUID id,
        String ownerName,

        String ownerEmail,
        BigDecimal balance,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
