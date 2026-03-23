package com.task.transactionservice.service;

import com.task.transactionservice.client.WalletClient;
import com.task.transactionservice.dto.*;
import com.task.transactionservice.enums.TransactionStatus;
import com.task.transactionservice.enums.TransactionType;
import com.task.transactionservice.exception.TransactionException;
import com.task.transactionservice.exception.WalletServiceException;
import com.task.transactionservice.model.Transaction;
import com.task.transactionservice.repository.TransactionRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletClient walletClient;

    @Override
    public TransactionResponse deposit(DepositRequest request) {
        log.info("Deposit request — walletId: {}, amount: {}", request.walletId(), request.amount());
        try {
            log.info("Calling wallet-service to credit walletId: {}", request.walletId());
            walletClient.credit(request.walletId(),
                    new BalanceUpdateRequest(request.amount()));
        } catch (FeignException e) {
            log.error("Failed to reach wallet-service during deposit — walletId: {}, error: {}", request.walletId(), e.getMessage());
            throw new WalletServiceException("Failed to reach wallet service: " + e.getMessage());
        }

        Transaction transaction = Transaction.builder()
                .walletId(request.walletId())
                .type(TransactionType.DEPOSIT)
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .description(request.description() != null ? request.description() : "Deposit")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Deposit successful — transactionId: {}, walletId: {}, amount: {}", saved.getId(), saved.getWalletId(), saved.getAmount());
        return mapToResponse(saved);
    }

    @Override
    public TransactionResponse withdraw(WithdrawRequest request) {
        log.info("Withdrawal request — walletId: {}, amount: {}", request.walletId(), request.amount());
        try {
            log.info("Calling wallet-service to debit walletId: {}", request.walletId());
            walletClient.debit(request.walletId(),
                    new BalanceUpdateRequest(request.amount()));
        } catch (FeignException.BadRequest e) {
            log.warn("Withdrawal rejected — insufficient funds in walletId: {}", request.walletId());
            throw new TransactionException("Insufficient funds in wallet");
        } catch (FeignException e) {
            log.error("Failed to reach wallet-service during withdrawal — walletId: {}, error: {}", request.walletId(), e.getMessage());
            throw new WalletServiceException("Failed to reach wallet service: " + e.getMessage());
        }

        Transaction transaction = Transaction.builder()
                .walletId(request.walletId())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .description(request.description() != null ? request.description() : "Withdrawal")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Withdrawal successful — transactionId: {}, walletId: {}, amount: {}", saved.getId(), saved.getWalletId(), saved.getAmount());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Transfer request — sourceWalletId: {}, targetWalletId: {}, amount: {}", request.sourceWalletId(), request.targetWalletId(), request.amount());

        if (request.sourceWalletId().equals(request.targetWalletId())) {
            log.warn("Transfer rejected — source and target wallet are the same: {}", request.sourceWalletId());
            throw new TransactionException("Cannot transfer funds to the same wallet");
        }

        // Verify target wallet exists before debiting source
        try {
            log.info("Verifying target wallet exists — targetWalletId: {}", request.targetWalletId());
            walletClient.getWalletById(request.targetWalletId());
        } catch (FeignException.NotFound e) {
            log.warn("Transfer rejected — target wallet not found: {}", request.targetWalletId());
            throw new TransactionException("Target wallet not found");
        }

        // Debit source wallet
        try {
            log.info("Debiting source wallet — sourceWalletId: {}, amount: {}", request.sourceWalletId(), request.amount());
            walletClient.debit(request.sourceWalletId(),
                    new BalanceUpdateRequest(request.amount()));
        } catch (FeignException.BadRequest e) {
            log.warn("Transfer rejected — insufficient funds in sourceWalletId: {}", request.sourceWalletId());
            throw new TransactionException("Insufficient funds in source wallet");
        } catch (FeignException e) {
            log.error("Failed to debit source wallet during transfer — sourceWalletId: {}, error: {}", request.sourceWalletId(), e.getMessage());
            throw new WalletServiceException("Failed to reach wallet service: " + e.getMessage());
        }

        // Credit target wallet
        try {
            log.info("Crediting target wallet — targetWalletId: {}, amount: {}", request.targetWalletId(), request.amount());
            walletClient.credit(request.targetWalletId(),
                    new BalanceUpdateRequest(request.amount()));
        } catch (FeignException e) {
            log.error("Credit step failed after debit — manual intervention may be needed — targetWalletId: {}, error: {}", request.targetWalletId(), e.getMessage());
            // TODO: compensating by creditingg source wallet back - why? Sga pattern needed in productionn
            throw new WalletServiceException("Transfer failed at credit step: " + e.getMessage());
        }

        Transaction transaction = Transaction.builder()
                .walletId(request.sourceWalletId())
                .targetWalletId(request.targetWalletId())
                .type(TransactionType.TRANSFER)
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .description(request.description() != null ? request.description() : "Transfer")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transfer successful — transactionId: {}, from: {}, to: {}, amount: {}", saved.getId(), saved.getWalletId(), saved.getTargetWalletId(), saved.getAmount());
        return mapToResponse(saved);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(UUID walletId) {
        log.info("Fetching transaction history for walletId: {}", walletId);
        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        log.info("Found {} transactions for walletId: {}", transactions.size(), walletId);
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getWalletId(),
                transaction.getTargetWalletId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
