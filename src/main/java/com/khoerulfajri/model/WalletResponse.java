package com.khoerulfajri.model;

import com.khoerulfajri.entity.Pengguna;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WalletResponse {
    private String id;
    private String walletNumber;
    private Long balance;
    private Pengguna pengguna;
    private Boolean isActive;
}
