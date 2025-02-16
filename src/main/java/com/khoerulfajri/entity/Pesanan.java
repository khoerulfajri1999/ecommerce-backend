package com.khoerulfajri.entity;

import com.khoerulfajri.model.StatusPesanan;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Pesanan implements Serializable {

    @Id
    private String id;
    private String nomor;

    @Temporal(TemporalType.DATE)
    private Date tanggal;

    @ManyToOne
    @JoinColumn
    private Pengguna pengguna;

    private String alamatPengiriman;
    private BigDecimal jumlah;
    private BigDecimal ongkir;
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private StatusPesanan statusPesanan;

    @Temporal(TemporalType.TIMESTAMP)
    private Date waktuPesan;

    @Getter
    @OneToMany(mappedBy = "pesanan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PesananItem> pesananItems;

}
