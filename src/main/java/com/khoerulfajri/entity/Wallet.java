package com.khoerulfajri.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "pengguna_id", referencedColumnName = "id")
    private Pengguna pengguna;

    @Column(name = "balance")
    private Long balance;

    @Column(name = "wallet_number", nullable = false, unique = true)
    private String walletNumber;

    @Column(name = "is_active")
    private Boolean isActive;
}
