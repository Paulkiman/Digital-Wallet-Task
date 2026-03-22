package com.task.walletservice.controller;

import com.task.walletservice.common.ApiResponse;
import com.task.walletservice.dto.BalanceUpdateRequest;
import com.task.walletservice.dto.WalletRequest;
import com.task.walletservice.dto.WalletResponse;
import com.task.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@Valid @RequestBody WalletRequest request) {

        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable UUID id) {

        WalletResponse response = walletService.getWalletById(id);
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved successfully", response));
    }

    // we are calling credit method via Feign internally by transaction-service
    @PostMapping("/{id}/credit")
    public ResponseEntity<ApiResponse<WalletResponse>> credit(@PathVariable UUID id, @Valid @RequestBody BalanceUpdateRequest request) {

        WalletResponse response = walletService.credit(id, request);
        return ResponseEntity.ok(ApiResponse.success("Credit applied successfully", response));
    }

    // we are also callingg this method internally via Feign by transaction-service
    @PostMapping("/{id}/debit")
    public ResponseEntity<ApiResponse<WalletResponse>> debit(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceUpdateRequest request) {

        WalletResponse response = walletService.debit(id, request);
        return ResponseEntity.ok(ApiResponse.success("Debit applied successfully", response));
    }
}