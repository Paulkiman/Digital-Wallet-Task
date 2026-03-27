package com.task.walletservice.repository;

import com.task.walletservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    boolean existsByOwnerEmail(String ownerEmail);
}
