package com.task.walletservice.service;

import com.task.walletservice.dto.BalanceUpdateRequest;
import com.task.walletservice.dto.WalletRequest;
import com.task.walletservice.dto.WalletResponse;

import java.util.UUID;

public interface WalletService {
    WalletResponse createWallet(WalletRequest request);
    WalletResponse getWalletById(UUID id);
    WalletResponse credit(UUID id, BalanceUpdateRequest request);
    WalletResponse debit(UUID id, BalanceUpdateRequest request);
}
