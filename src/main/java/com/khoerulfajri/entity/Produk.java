package com.khoerulfajri.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
public class Produk implements Serializable {

    @Id
    private String id;
    private String nama;
    private String deskripsi;
    private String gambar;
    private BigDecimal harga;
    private Integer stok;
    @JoinColumn
    @ManyToOne
    private Kategori kategori;
    @JoinColumn
    @ManyToOne
    private Pengguna pengguna;

}
