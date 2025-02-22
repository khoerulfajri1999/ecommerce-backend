package com.khoerulfajri.service;

import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.entity.Transaksi;
import com.khoerulfajri.entity.Wallet;
import com.khoerulfajri.exception.BadRequestException;
import com.khoerulfajri.exception.ResourceNotFoundException;
import com.khoerulfajri.model.*;
import com.khoerulfajri.repository.PenggunaRepository;
import com.khoerulfajri.repository.TransaksiRepository;
import com.khoerulfajri.repository.WalletRepository;
import com.khoerulfajri.security.service.UserDetailsImpl;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static java.lang.Integer.parseInt;

@Service
public class TransaksiService {
    private final TransaksiRepository transaksiRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final RestClient restClient;
    private final String MIDTRANS_SERVER_KEY;
    private final String MIDTRANS_API_URL;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String MIDTRANS_API_URL2;
    @Autowired
    private PenggunaRepository penggunaRepository;
//    payment.url2
    @Autowired
    public TransaksiService(
            TransaksiRepository transaksiRepository,
            WalletRepository walletRepository,
            RestClient restClient,
            WalletService walletService,
            @Value("${payment.secret-key}") String MIDTRANS_SERVER_KEY,
            @Value("${payment.url}") String MIDTRANS_API_URL,
            @Value("${payment.url2}") String MIDTRANS_API_URL2) {
        this.transaksiRepository = transaksiRepository;
        this.walletService = walletService;
        this.walletRepository = walletRepository;
        this.restClient = restClient;
        this.MIDTRANS_SERVER_KEY = MIDTRANS_SERVER_KEY;
        this.MIDTRANS_API_URL = MIDTRANS_API_URL;
        this.MIDTRANS_API_URL2 = MIDTRANS_API_URL2;
    }

    @Transactional(rollbackFor = Exception.class)
    public Transaksi topUp(String id, Long amount) {

        Pengguna pengguna = penggunaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pengguna tidak ditemukan"));

        Wallet wallet = walletRepository.findByPenggunaId(pengguna.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Transaksi transaksi = Transaksi.builder()
                .wallet(wallet)
                .build();
        transaksiRepository.saveAndFlush(transaksi);
        PaymentRequest request = PaymentRequest.builder()
                .paymentDetail(
                        PaymentDetailRequest.builder()
                                .orderId(transaksi.getId())
                                .grossAmount(amount)
                                .build()
                )
                .penggunaRequest(
                        PenggunaRequest.builder()
                                .id(pengguna.getId())
                                .nama(pengguna.getNama())
                                .alamat(pengguna.getAlamat())
                                .gambar(pengguna.getGambar())
                                .email(pengguna.getEmail())
                                .hp(pengguna.getHp())
                                .build()
                )
                .build();
        ResponseEntity<Map<String, String>> response = restClient.post()
                .uri(MIDTRANS_API_URL)
                .body(request)
                .header("Authorization", "Basic " + MIDTRANS_SERVER_KEY)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
        Map<String, String> body = response.getBody();
        transaksi = Transaksi.builder()
                .id(transaksi.getId())
                .token(body.get("token"))
                .redirectUrl(body.get("redirect_url"))
                .vaNumber(body.get("va_number"))
                .statusTransaksi(StatusTransaksi.ORDERED)
                .build();

        return updateTransaksi(transaksi);
    }

    public Transaksi updateTransaksi(Transaksi transaksi){
        Transaksi existingTransaksi = transaksiRepository.findById(transaksi.getId()).orElseThrow(()-> new ResourceNotFoundException("id transaksi tidak ditemukan"));
        if (existingTransaksi.getStatusTransaksi() == StatusTransaksi.SETTLEMENT){
            throw new BadRequestException("Transaksi sudah dilakukan");
        }
        existingTransaksi.setStatusTransaksi(transaksi.getStatusTransaksi());
        existingTransaksi.setToken(transaksi.getToken());
        existingTransaksi.setRedirectUrl(transaksi.getRedirectUrl());
        existingTransaksi.setVaNumber(transaksi.getVaNumber());
        return transaksiRepository.saveAndFlush(existingTransaksi);
    }

    @Transactional(rollbackFor = Exception.class)
    public String getTransactionStatus(String orderId) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MIDTRANS_API_URL2 + orderId + "/status"))
                .header("accept", "application/json")
                .header("Authorization", "Basic " + MIDTRANS_SERVER_KEY)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject jsonObject = new JSONObject(response.body());
        if (parseInt(jsonObject.getString("status_code")) != 200) {
            throw new BadRequestException("Transaksi gagal");
        }
        if (StatusTransaksi.valueOf((jsonObject.getString("transaction_status").toUpperCase())) != StatusTransaksi.SETTLEMENT) {
            throw new BadRequestException("Transaksi sudah dilakukan");
        }

        Wallet wallet = walletRepository.findById(transaksiRepository.findById(orderId).get().getWallet().getId())
                .orElseThrow(() -> new RuntimeException("Wallet dengan ID tidak ditemukan"));

        BigDecimal grossAmount = new BigDecimal(jsonObject.getString("gross_amount"));
        WalletRequest requestWallet = WalletRequest.builder()
                        .id(wallet.getId())
                        .balance(wallet.getBalance() + grossAmount.longValue())
                        .build();
        walletService.updateWallet(requestWallet);

        updateTransaksi(Transaksi.builder()
                .id(orderId)
                .statusTransaksi(StatusTransaksi.valueOf((jsonObject.getString("transaction_status").toUpperCase())))
                .build());
        return response.body();
    }
}
