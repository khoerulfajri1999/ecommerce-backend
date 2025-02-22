package com.khoerulfajri.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WalletRequest {
    private String id;
    private String penggunaId;
    private Long balance;
    private String walletNumber;
}
