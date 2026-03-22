package com.task.walletservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record WalletRequest(

        @NotBlank(message = "Owner name is required")
        String ownerName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email")
        String ownerEmail

) {}
