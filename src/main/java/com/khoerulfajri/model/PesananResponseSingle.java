package com.khoerulfajri.model;

import com.khoerulfajri.entity.Pesanan;
import com.khoerulfajri.entity.PesananItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class PesananResponseSingle implements Serializable {

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

    public PesananResponseSingle(Pesanan pesanan, List<PesananItem> pesananItems) {
        this.id = pesanan.getId();
        this.nomorPesanan = pesanan.getNomor();
        this.tanggal = pesanan.getTanggal();
        this.namaPelanggan = pesanan.getPengguna().getNama();
        this.alamatPengiriman = pesanan.getAlamatPengiriman();
        this.waktuPesan = pesanan.getWaktuPesan();
        this.jumlah = pesanan.getJumlah();
        this.ongkir = pesanan.getOngkir();
        this.total = pesanan.getTotal();
        this.statusPesanan = pesanan.getStatusPesanan();
        items = new ArrayList<>();
        for (PesananItem pesananItem : pesananItems) {
            Item item = new Item();
            item.setProdukId(pesananItem.getProduk().getId());
            item.setNamaProduk(pesananItem.getDeskripsi());
            item.setKuantitas(pesananItem.getKuantitas());
            item.setHarga(pesananItem.getHarga());
            item.setJumlah(pesananItem.getJumlah());
            items.add(item);
        }
    }

    @Data
    public static class Item implements Serializable {
        private String produkId;
        private String namaProduk;
        private Integer kuantitas;
        private BigDecimal harga;
        private BigDecimal jumlah;
    }

}
