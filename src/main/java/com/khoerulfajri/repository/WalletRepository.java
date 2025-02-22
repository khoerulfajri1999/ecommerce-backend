package com.khoerulfajri.repository;

import com.khoerulfajri.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
    Optional<Wallet> findByWalletNumber(String walletNumber);

    Optional<Wallet> findByPenggunaId(String penggunaId);
}
