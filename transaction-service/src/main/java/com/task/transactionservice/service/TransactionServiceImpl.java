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
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletClient walletClient;

    @Override
    public TransactionResponse deposit(DepositRequest request) {
        try {
            walletClient.credit(request.walletId(),
                    new BalanceUpdateRequest(request.amount()));
        } catch (FeignException e) {
            throw new WalletServiceException("Failed to reach wallet service: " + e.getMessage());
        }

        Transaction transaction = Transaction.builder()
                .walletId(request.walletId())
                .type(TransactionType.DEPOSIT)
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .description(request.description() != null ? request.description() : "Deposit")
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Override
    public TransactionResponse withdraw(WithdrawRequest request) {
        try {
            walletClient.debit(request.walletId(),
                    new BalanceUpdateRequest(request.amount()));
        } catch (FeignException.BadRequest e) {
            throw new TransactionException("Insufficient funds in wallet");
        } catch (FeignException e) {
            throw new WalletServiceException("Failed to reach wallet service: " + e.getMessage());
        }

        Transaction transaction = Transaction.builder()
                .walletId(request.walletId())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .description(request.description() != null ? request.description() : "Withdrawal")
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        if (request.sourceWalletId().equals(request.targetWalletId())) {
            throw new TransactionException("Cannot transfer funds to the same wallet");
        }

        // Verify target wallet exists before debiting source
        try {
            walletClient.getWalletById(request.targetWalletId());
        } catch (FeignException.NotFound e) {
            throw new TransactionException("Target wallet not found");
        }

        // Debit source wallet
        try {
            walletClient.debit(request.sourceWalletId(),
                    new BalanceUpdateRequest(request.amount()));
        } catch (FeignException.BadRequest e) {
            throw new TransactionException("Insufficient funds in source wallet");
        } catch (FeignException e) {
            throw new WalletServiceException("Failed to reach wallet service: " + e.getMessage());
        }

        // Credit target wallet
        try {
            walletClient.credit(request.targetWalletId(),
                    new BalanceUpdateRequest(request.amount()));
        } catch (FeignException e) {
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

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(UUID walletId) {
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
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
