package com.khoerulfajri.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.khoerulfajri.model.StatusPesanan;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private String namaPenjual;
    @Column(name = "token")
    private String token;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Column(name = "va_number")
    private String vaNumber;

    @Enumerated(EnumType.STRING)
    private StatusPesanan statusPesanan;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Jakarta")
    private Date waktuPesan;

    @Getter
    @OneToMany(mappedBy = "pesanan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PesananItem> pesananItems;

}
