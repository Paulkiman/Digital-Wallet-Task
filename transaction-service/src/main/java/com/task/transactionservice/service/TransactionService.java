package com.task.transactionservice.service;

import com.task.transactionservice.dto.DepositRequest;
import com.task.transactionservice.dto.TransactionResponse;
import com.task.transactionservice.dto.TransferRequest;
import com.task.transactionservice.dto.WithdrawRequest;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransactionResponse deposit(DepositRequest request);
    TransactionResponse withdraw(WithdrawRequest request);
    TransactionResponse transfer(TransferRequest request);
    List<TransactionResponse> getTransactionHistory(UUID walletId);
}
