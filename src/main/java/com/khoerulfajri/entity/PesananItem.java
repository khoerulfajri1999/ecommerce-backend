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
public class PesananItem implements Serializable {

    @Id
    private String id;
    @JoinColumn
    @ManyToOne
    private Produk produk;
    @JoinColumn
    @ManyToOne
    private Pesanan pesanan;
    private String deskripsi;
    private Integer kuantitas;
    private BigDecimal harga;
    private BigDecimal jumlah;

}
