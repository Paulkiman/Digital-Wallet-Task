package com.task.transactionservice.client;

import com.task.transactionservice.common.ApiResponse;
import com.task.transactionservice.dto.BalanceUpdateRequest;
import com.task.transactionservice.dto.WalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(name = "wallet-service", url = "${wallet.service.url}")
public interface WalletClient {

    @GetMapping("/api/wallets/{id}")
    ApiResponse<WalletResponse> getWalletById(@PathVariable UUID id);

    @PostMapping("/api/wallets/{id}/credit")
    ApiResponse<WalletResponse> credit(@PathVariable UUID id,
                                       @RequestBody BalanceUpdateRequest request);

    @PostMapping("/api/wallets/{id}/debit")
    ApiResponse<WalletResponse> debit(@PathVariable UUID id,
                                      @RequestBody BalanceUpdateRequest request);
}
