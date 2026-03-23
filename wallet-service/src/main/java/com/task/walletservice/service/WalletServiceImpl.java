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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    public WalletResponse createWallet(WalletRequest request) {
        log.info("Request to create wallet for email: {}", request.ownerEmail());

        if (walletRepository.existsByOwnerEmail(request.ownerEmail())) {
            log.warn("Wallet creation failed — email already exists: {}", request.ownerEmail());
            throw new WalletAlreadyExistsException(
                    "Wallet already exists for email: " + request.ownerEmail()
            );
        }

        Wallet wallet = Wallet.builder()
                .ownerName(request.ownerName())
                .ownerEmail(request.ownerEmail())
                .build();

        Wallet saved = walletRepository.save(wallet);
        log.info("Wallet created successfully — id: {}, email: {}", saved.getId(), saved.getOwnerEmail());
        return mapToResponse(saved);
    }

    @Override
    public WalletResponse getWalletById(UUID id) {
        log.info("Fetching wallet with id: {}", id);
        return mapToResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public WalletResponse credit(UUID id, BalanceUpdateRequest request) {
        //TODO : adding of idempotency key check here laterr on, to prevent duplicate credits
        log.info("Credit request — walletId: {}, amount: {}", id, request.amount());
        Wallet wallet = findOrThrow(id);
        wallet.setBalance(wallet.getBalance().add(request.amount()));
        Wallet saved = walletRepository.save(wallet);
        log.info("Credit successful — walletId: {}, new balance: {}", id, saved.getBalance());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public WalletResponse debit(UUID id, BalanceUpdateRequest request) {
        log.info("Debit request — walletId: {}, amount: {}", id, request.amount());
        Wallet wallet = findOrThrow(id);

        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            log.warn("Insufficient funds — walletId: {}, available: {}, requested: {}", id, wallet.getBalance(), request.amount());
            throw new InsufficientFundsException(
                    "Insufficient funds. Available balance: " + wallet.getBalance()
            );
        }

        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        Wallet saved = walletRepository.save(wallet);
        log.info("Debit successful — walletId: {}, new balance: {}", id, saved.getBalance());
        return mapToResponse(saved);
    }

    private Wallet findOrThrow(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Wallet not found with id: {}", id);
                    return new WalletNotFoundException("Wallet not found with id: " + id);
                });
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
