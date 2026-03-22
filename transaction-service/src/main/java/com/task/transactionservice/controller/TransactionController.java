package com.task.transactionservice.controller;

import com.task.transactionservice.common.ApiResponse;
import com.task.transactionservice.dto.DepositRequest;
import com.task.transactionservice.dto.TransactionResponse;
import com.task.transactionservice.dto.TransferRequest;
import com.task.transactionservice.dto.WithdrawRequest;
import com.task.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {

        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit successful", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {

        TransactionResponse response = transactionService.withdraw(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal successful", response));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {

        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer successful", response));
    }

    @GetMapping("/history/{walletId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistory(@PathVariable UUID walletId) {

        List<TransactionResponse> response = transactionService.getTransactionHistory(walletId);
        return ResponseEntity.ok(ApiResponse.success("Transaction history retrieved", response));
    }
}