package com.khoerulfajri.controller;

import com.khoerulfajri.entity.Transaksi;
import com.khoerulfajri.entity.Wallet;
import com.khoerulfajri.model.CommonResponse;
import com.khoerulfajri.model.TopUpRequest;
import com.khoerulfajri.repository.TransaksiRepository;
import com.khoerulfajri.security.service.UserDetailsImpl;
import com.khoerulfajri.service.TransaksiService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController

@RequestMapping("/api/payment")
//@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransaksiController {

    private final TransaksiService transactionService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/topup")
    public ResponseEntity<CommonResponse<Transaksi>> topUp(@AuthenticationPrincipal UserDetailsImpl user,@RequestBody TopUpRequest request) {

        Transaksi transaksi = transactionService.topUp(user.getUsername(), request.getAmount());
        CommonResponse<Transaksi> response = CommonResponse.<Transaksi>builder()
                .statusCode(200)
                .message("Top up berhasil")
                .data(transaksi)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response);

    }
    @GetMapping("/status/{orderId}")
    public String getTransactionStatus(@PathVariable String orderId) throws IOException, InterruptedException {
        return transactionService.getTransactionStatus(orderId);
    }

    @PostMapping("/notification")
    public ResponseEntity<String> handleNotification(@RequestBody String payload) throws IOException, InterruptedException {
        System.out.println("Midtrans Notification: " + payload);
        JSONObject jsonObject = new JSONObject(payload);
        String transactionId = jsonObject.getString("order_id");
        getTransactionStatus(transactionId);
        return ResponseEntity.ok("Notification received");
    }
}
