package com.task.transactionservice.dto;

import com.task.transactionservice.enums.TransactionStatus;
import com.task.transactionservice.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID walletId,

        UUID targetWalletId,

        TransactionType type,
        BigDecimal amount,
        TransactionStatus status,

        String description,
        LocalDateTime createdAt

) {}
