package com.task.walletservice.service;

import com.task.walletservice.dto.BalanceUpdateRequest;
import com.task.walletservice.dto.WalletRequest;
import com.task.walletservice.dto.WalletResponse;
import com.task.walletservice.exception.InsufficientFundsException;
import com.task.walletservice.exception.WalletAlreadyExistsException;
import com.task.walletservice.exception.WalletNotFoundException;
import com.task.walletservice.model.Wallet;
import com.task.walletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    public WalletResponse createWallet(WalletRequest request) {
        if (walletRepository.existsByOwnerEmail(request.ownerEmail())) {
            throw new WalletAlreadyExistsException(
                    "Wallet already exists for email: " + request.ownerEmail()
            );
        }

        Wallet wallet = Wallet.builder()
                .ownerName(request.ownerName())
                .ownerEmail(request.ownerEmail())
                .build();

        return mapToResponse(walletRepository.save(wallet));
    }

    @Override
    public WalletResponse getWalletById(UUID id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public WalletResponse credit(UUID id, BalanceUpdateRequest request) {
        //TODO : adding of idempotency key check here, to prevent duplicate credits
        Wallet wallet = findOrThrow(id);
        wallet.setBalance(wallet.getBalance().add(request.amount()));
        return mapToResponse(walletRepository.save(wallet));
    }

    @Override
    @Transactional
    public WalletResponse debit(UUID id, BalanceUpdateRequest request) {
        Wallet wallet = findOrThrow(id);

        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available balance: " + wallet.getBalance()
            );
        }

        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        return mapToResponse(walletRepository.save(wallet));
    }

    private Wallet findOrThrow(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + id));
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getOwnerName(),
                wallet.getOwnerEmail(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}