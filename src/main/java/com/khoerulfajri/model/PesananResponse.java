package com.khoerulfajri.model;

import com.khoerulfajri.entity.Pesanan;
import com.khoerulfajri.entity.PesananItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PesananResponse implements Serializable {

    private String id;
    private String nomorPesanan;
    private Date tanggal;
    private String namaPelanggan;
    private String alamatPengiriman;
    private Date waktuPesan;
    private BigDecimal jumlah;
    private BigDecimal ongkir;
    private BigDecimal total;
    private StatusPesanan statusPesanan;
    private List<Item> items;

    public PesananResponse(Pesanan pesanan, List<PesananItem> pesananItems) {
        this.id = pesanan.getId();
        this.nomorPesanan = pesanan.getNomor();
        this.tanggal = pesanan.getTanggal();
        this.namaPelanggan = Optional.ofNullable(pesanan.getPengguna())
                .map(p -> p.getNama())
                .orElse("Tidak Diketahui");
        this.alamatPengiriman = pesanan.getAlamatPengiriman();
        this.waktuPesan = pesanan.getWaktuPesan();
        this.jumlah = pesanan.getJumlah();
        this.ongkir = pesanan.getOngkir();
        this.total = pesanan.getTotal();
        this.statusPesanan = pesanan.getStatusPesanan();

        // Menggunakan Stream API untuk pembuatan List lebih efisien
        this.items = pesananItems.stream().map(pesananItem -> new Item(
                pesananItem.getProduk().getId(),
                pesananItem.getDeskripsi(),
                pesananItem.getKuantitas(),
                pesananItem.getHarga(),
                pesananItem.getJumlah()
        )).collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item implements Serializable {
        private String produkId;
        private String namaProduk;
        private Integer kuantitas;
        private BigDecimal harga;
        private BigDecimal jumlah;
    }

}
