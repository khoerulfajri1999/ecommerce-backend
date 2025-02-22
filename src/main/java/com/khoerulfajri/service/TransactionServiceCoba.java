//package com.khoerulfajri.service;
//
//import com.khoerulfajri.entity.Pengguna;
//import com.khoerulfajri.entity.Transaksi;
//import com.khoerulfajri.entity.Wallet;
//import com.khoerulfajri.exception.BadRequestException;
//import com.khoerulfajri.model.StatusTransaksi;
//import com.khoerulfajri.repository.PenggunaRepository;
//import com.khoerulfajri.repository.TransaksiRepository;
//import com.khoerulfajri.repository.WalletRepository;
//import com.khoerulfajri.security.service.UserDetailsImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.HttpStatusCodeException;
//import org.springframework.web.client.RestTemplate;
//
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//
//@Service
//public class TransactionServiceCoba {
//    private final TransaksiRepository transaksiRepository;
//    private final WalletRepository walletRepository;
//    private final RestTemplate restTemplate;
//    private final String MIDTRANS_SERVER_KEY;
//    private final String MIDTRANS_API_URL;
//    @Autowired
//    private PenggunaRepository penggunaRepository;
//
//    @Autowired
//    public TransactionServiceCoba(
//            TransaksiRepository transaksiRepository,
//            WalletRepository walletRepository,
//            RestTemplate restTemplate,
//            @Value("${payment.secret-key}") String MIDTRANS_SERVER_KEY,
//            @Value("${payment.url}") String MIDTRANS_API_URL) {
//        this.transaksiRepository = transaksiRepository;
//        this.walletRepository = walletRepository;
//        this.restTemplate = restTemplate;
//        this.MIDTRANS_SERVER_KEY = MIDTRANS_SERVER_KEY;
//        this.MIDTRANS_API_URL = MIDTRANS_API_URL;
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    public Transaksi topUp(Long amount) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
//            throw new BadRequestException("Pengguna tidak valid atau tidak terautentikasi");
//        }
//
//        String id = ((UserDetailsImpl) auth.getPrincipal()).getUsername();
//        Pengguna pengguna = penggunaRepository.findById(id)
//                .orElseThrow(() -> new BadRequestException("Pengguna tidak ditemukan"));
//
//        Wallet wallet = walletRepository.findByPenggunaId(pengguna.getId())
//                .orElseThrow(() -> new RuntimeException("Wallet not found"));
//
//        // Tidak menyimpan transaksi dulu sebelum respons Midtrans diterima
//        Transaksi transaksi = Transaksi.builder()
//                .wallet(wallet)
//                .statusTransaksi(StatusTransaksi.ORDERED)
//                .build();
//
//        try {
//            // Buat request body untuk Midtrans
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("transaction_details", Map.of(
//                    "order_id", UUID.randomUUID().toString(),  // Pastikan order_id unik
//                    "gross_amount", amount
//            ));
//            payload.put("payment_type", "bank_transfer");
//            payload.put("bank_transfer", Map.of("bank", "bca"));
//            payload.put("customer_details", Map.of(
//                    "first_name", pengguna.getNama(),
//                    "email", pengguna.getEmail()
//            ));
//
//            // Buat header request
//            String serverKey = "SB-Mid-server-p7DZOWSd-w9Ns0Mz-_dKIT1V";
//            String encodedKey = Base64.getEncoder().encodeToString((serverKey + ":").getBytes(StandardCharsets.UTF_8));
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Basic " + encodedKey);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
//
//            // Kirim request ke Midtrans
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    "https://api.sandbox.midtrans.com/v2/charge",
//                    HttpMethod.POST,
//                    requestEntity,
//                    Map.class
//            );
//
//            // Ambil response dari Midtrans
//            Map<String, Object> responseBody = response.getBody();
//            if (responseBody == null) {
//                throw new RuntimeException("Response dari Midtrans kosong");
//            }
//
//            // Debugging: print response Midtrans
//            System.out.println("Midtrans Response: " + responseBody);
//
//            // Ambil token, redirect_url, atau va_numbers
//            String token = (String) responseBody.get("token");
//            String redirectUrl = (String) responseBody.get("redirect_url");
//
//            // Jika pembayaran bank transfer, ambil Virtual Account (VA) number
//            List<Map<String, String>> vaNumbers = (List<Map<String, String>>) responseBody.get("va_numbers");
//            String vaNumber = (vaNumbers != null && !vaNumbers.isEmpty()) ? vaNumbers.get(0).get("va_number") : null;
//
//            if (token == null && redirectUrl == null && vaNumber == null) {
//                throw new RuntimeException("Midtrans tidak mengembalikan token, redirect_url, atau va_number");
//            }
//
//            // Simpan transaksi hanya jika response valid
//            transaksi.setToken(token);
//            transaksi.setRedirectUrl(redirectUrl);
//            transaksi.setVaNumber(vaNumber);  // Simpan Virtual Account jika ada
//            transaksi.setStatusTransaksi(StatusTransaksi.ORDERED);
//            transaksiRepository.save(transaksi);
//
//            return transaksi;
//        } catch (HttpStatusCodeException ex) {
//            System.out.println("Midtrans Error: " + ex.getResponseBodyAsString());
//            throw new RuntimeException("Payment failed: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
//        } catch (Exception e) {
//            throw new RuntimeException("Payment failed: " + e.getMessage());
//        }
//    }
//
//
//
//    private StatusTransaksi convertMidtransStatus(String midtransStatus) {
//        return switch (midtransStatus) {
//            case "settlement" -> StatusTransaksi.SETTELMENT;
//            case "pending" -> StatusTransaksi.PENDING;
//            case "cancel" -> StatusTransaksi.CANCEL;
//            case "failure" -> StatusTransaksi.FAILURE;
//            case "expired" -> StatusTransaksi.EXPIRED;
//            case "deny" -> StatusTransaksi.DENY;
//            default -> StatusTransaksi.ORDERED; // Default ke ORDERED jika tidak diketahui
//        };
//    }
//
////    private String encodeBase64(String value) {
////        return java.util.Base64.getEncoder().encodeToString(value.getBytes());
////    }
//}
