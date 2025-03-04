package com.khoerulfajri.controller;

import com.khoerulfajri.entity.Wallet;
import com.khoerulfajri.model.WalletResponse;
import com.khoerulfajri.security.service.UserDetailsImpl;
import com.khoerulfajri.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public WalletResponse createWallet(@AuthenticationPrincipal UserDetailsImpl user, @RequestBody Wallet request) {
        return walletService.createWallet(user.getUsername(), request);
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
