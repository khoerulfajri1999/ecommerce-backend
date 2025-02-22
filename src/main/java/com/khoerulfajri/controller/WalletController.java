package com.khoerulfajri.controller;

import com.khoerulfajri.entity.Wallet;
import com.khoerulfajri.model.WalletResponse;
import com.khoerulfajri.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public WalletResponse createWallet(@RequestBody Wallet request) {
        return walletService.createWallet(request);
    }

    @GetMapping
    public WalletResponse getWalletByPenggunaId() {
        return walletService.getWalletByPenggunaId();
    }

    @DeleteMapping
    public void deleteWalletByPenggunaId() {
        walletService.deleteWalletByPenggunaId();
    }
}
