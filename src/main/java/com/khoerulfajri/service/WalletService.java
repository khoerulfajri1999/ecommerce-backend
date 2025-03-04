package com.khoerulfajri.service;

import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.entity.Wallet;
import com.khoerulfajri.exception.BadRequestException;
import com.khoerulfajri.model.WalletRequest;
import com.khoerulfajri.model.WalletResponse;
import com.khoerulfajri.repository.PenggunaRepository;
import com.khoerulfajri.repository.WalletRepository;
import com.khoerulfajri.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final PenggunaRepository penggunaRepository;

    public WalletResponse createWallet(String username, Wallet request) {

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
//            throw new BadRequestException("Pengguna tidak valid atau tidak terautentikasi");
//        }
//
//        String id = ((UserDetailsImpl) auth.getPrincipal()).getUsername();

        Pengguna pengguna = penggunaRepository.findById(username)
                .orElseThrow(() -> new BadRequestException("Pengguna tidak ditemukan"));

        Wallet wallet = Wallet.builder()
                .walletNumber(pengguna.getHp())
                .balance(0L)
                .pengguna(pengguna)
                .isActive(true)
                .build();
        Wallet createWallet = walletRepository.save(wallet);

        return WalletResponse.builder()
                .id(createWallet.getId())
                .balance(createWallet.getBalance())
                .pengguna(createWallet.getPengguna())
                .walletNumber(createWallet.getWalletNumber())
                .isActive(createWallet.getIsActive())
                .build();
    }

    public WalletResponse getWalletByPenggunaId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            throw new BadRequestException("Pengguna tidak valid atau tidak terautentikasi");
        }

        String penggunaId = ((UserDetailsImpl) auth.getPrincipal()).getUsername();

        Wallet wallet = walletRepository.findByPenggunaId(penggunaId)
                .orElseThrow(() -> new BadRequestException("Wallet tidak ditemukan"));

        return WalletResponse.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .pengguna(wallet.getPengguna())
                .walletNumber(wallet.getWalletNumber())
                .isActive(wallet.getIsActive())
                .build();
    }

    public void deleteWalletByPenggunaId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            throw new BadRequestException("Pengguna tidak valid atau tidak terautentikasi");
        }

        String penggunaId = ((UserDetailsImpl) auth.getPrincipal()).getUsername();

        Wallet wallet = walletRepository.findByPenggunaId(penggunaId)
                .orElseThrow(() -> new BadRequestException("Wallet tidak ditemukan"));
        walletRepository.delete(wallet);
    }

    public WalletResponse updateWallet(WalletRequest request) {

        Wallet wallet = walletRepository.findById(request.getId())
                .orElseThrow(() -> new BadRequestException("Wallet tidak ditemukan"));
        wallet.setBalance(request.getBalance());
        Wallet updateWallet = walletRepository.save(wallet);

        return WalletResponse.builder()
                .id(updateWallet.getId())
                .balance(updateWallet.getBalance())
                .pengguna(updateWallet.getPengguna())
                .walletNumber(updateWallet.getWalletNumber())
                .isActive(updateWallet.getIsActive())
                .build();
    }

}
