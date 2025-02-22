package com.khoerulfajri.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PenggunaRequest {
    private String id;
    private String nama;
    private String email;
    private String alamat;
    private String gambar;
    private String hp;
    private Boolean isAktif;
}
