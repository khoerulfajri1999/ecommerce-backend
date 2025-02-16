package com.khoerulfajri.model;

import com.khoerulfajri.entity.Kategori;
import com.khoerulfajri.entity.Pengguna;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ProdukResponse {
    private String id;
    private String nama;
    private String deskripsi;
    private String gambar;
    private BigDecimal harga;
    private Integer stok;
    private Kategori kategori;
    private Pengguna pengguna;

    public ProdukResponse(String id, String nama, String deskripsi, String gambar, BigDecimal harga, Integer stok, Kategori kategori, Pengguna pengguna) {
        this.id = id;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.gambar = gambar;
        this.harga = harga;
        this.stok = stok;
        this.kategori = kategori;
        this.pengguna = pengguna;
    }
}


