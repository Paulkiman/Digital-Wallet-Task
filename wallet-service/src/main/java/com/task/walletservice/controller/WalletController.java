package com.task.walletservice.controller;

import com.task.walletservice.common.ApiResponse;
import com.task.walletservice.dto.BalanceUpdateRequest;
import com.task.walletservice.dto.WalletRequest;
import com.task.walletservice.dto.WalletResponse;
import com.task.walletservice.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet Service", description = "Endpoints for managing digital wallets")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Create a new wallet")
    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@Valid @RequestBody WalletRequest request) {
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully", response));
    }

    @Operation(summary = "Get wallet by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable UUID id) {
        WalletResponse response = walletService.getWalletById(id);
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved successfully", response));
    }

    // we are calling credit method via Feign internally by transaction-service
    @Operation(summary = "Credit funds to a wallet")
    @PostMapping("/{id}/credit")
    public ResponseEntity<ApiResponse<WalletResponse>> credit(@PathVariable UUID id, @Valid @RequestBody BalanceUpdateRequest request) {
        WalletResponse response = walletService.credit(id, request);
        return ResponseEntity.ok(ApiResponse.success("Credit applied successfully", response));
    }

    // we are also callingg this method internally via Feign by transaction-service
    @Operation(summary = "Debit funds from a wallet")
    @PostMapping("/{id}/debit")
    public ResponseEntity<ApiResponse<WalletResponse>> debit(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceUpdateRequest request) {
        WalletResponse response = walletService.debit(id, request);
        return ResponseEntity.ok(ApiResponse.success("Debit applied successfully", response));
    }
}
