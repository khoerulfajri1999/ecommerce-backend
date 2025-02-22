package com.khoerulfajri.entity;

import com.khoerulfajri.model.StatusTransaksi;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Transaksi {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "token")
    private String token;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Column(name = "va_number")
    private String vaNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status")
    private StatusTransaksi statusTransaksi;

    @OneToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
}
