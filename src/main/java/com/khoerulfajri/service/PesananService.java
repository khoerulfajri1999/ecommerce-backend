package com.khoerulfajri.service;

import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.entity.Pesanan;
import com.khoerulfajri.entity.PesananItem;
import com.khoerulfajri.entity.Produk;
import com.khoerulfajri.exception.BadRequestException;
import com.khoerulfajri.exception.ResourceNotFoundException;
import com.khoerulfajri.model.*;
import com.khoerulfajri.repository.PesananItemRepository;
import com.khoerulfajri.repository.PesananRepository;
import com.khoerulfajri.repository.ProdukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PesananService implements Serializable {

    @Autowired
    private ProdukRepository produkRepository;
    @Autowired
    private PesananRepository pesananRepository;
    @Autowired
    private PesananItemRepository pesananItemRepository;
    @Autowired
    private KeranjangService keranjangService;
    @Autowired
    private PesananLogService pesananLogService;

    @Transactional
    public PesananResponse create(String username, PesananRequest request) {
        Pesanan pesanan = new Pesanan();
        pesanan.setId(UUID.randomUUID().toString());
        pesanan.setTanggal(new Date());
        pesanan.setNomor(generateNomorPesanan());
        pesanan.setPengguna(new Pengguna(username));
        pesanan.setAlamatPengiriman(request.getAlamatPengiriman());
        pesanan.setStatusPesanan(StatusPesanan.DRAFT);
        pesanan.setWaktuPesan(new Date());

        List<PesananItem> items = new ArrayList<>();
        for (KeranjangRequest k : request.getItems()) {
            Produk produk = produkRepository.findById(k.getProdukId())
                    .orElseThrow(() -> new BadRequestException("Produk ID " + k.getProdukId() + " tidak ditemukan"));
            if (produk.getStok() < k.getKuantitas()) {
                throw new BadRequestException("Stok tidak mencukupi");
            }

            PesananItem pi = new PesananItem();
            pi.setId(UUID.randomUUID().toString());
            pi.setProduk(produk);
            pi.setDeskripsi(produk.getNama());
            pi.setKuantitas(k.getKuantitas());
            pi.setHarga(produk.getHarga());
            pi.setJumlah(new BigDecimal(pi.getHarga().doubleValue() * pi.getKuantitas()));
            pi.setPesanan(pesanan);
            items.add(pi);
        }

        BigDecimal jumlah = BigDecimal.ZERO;
        for (PesananItem pesananItem : items) {
            jumlah = jumlah.add(pesananItem.getJumlah());
        }

        pesanan.setJumlah(jumlah);
        pesanan.setOngkir(request.getOngkir());
        pesanan.setTotal(pesanan.getJumlah().add(pesanan.getOngkir()));

        Pesanan saved = pesananRepository.save(pesanan);
        for (PesananItem pesananItem : items) {
            pesananItemRepository.save(pesananItem);
            Produk produk = pesananItem.getProduk();
            produk.setStok(produk.getStok() - pesananItem.getKuantitas());
            produkRepository.save(produk);
            keranjangService.deleteKeranjang(username, produk.getId());
        }

        // catat log
        pesananLogService.createLog(username, pesanan, PesananLogService.DRAFT, "Pesanan sukses dibuat");
        PesananResponse pesananResponse = new PesananResponse(saved, items);
        return pesananResponse;

    }

    @Transactional
    public PesananResponse createPesananSingle(String username, PesananRequestSingle request) {
        Pesanan pesanan = new Pesanan();
        pesanan.setId(UUID.randomUUID().toString());
        pesanan.setTanggal(new Date());
        pesanan.setNomor(generateNomorPesanan());
        pesanan.setPengguna(new Pengguna(username));
        pesanan.setAlamatPengiriman(request.getAlamatPengiriman());
        pesanan.setStatusPesanan(StatusPesanan.DRAFT);
        pesanan.setWaktuPesan(new Date());

        // Ambil produk dari database
        Produk produk = produkRepository.findById(request.getProdukId())
                .orElseThrow(() -> new BadRequestException("Produk ID " + request.getProdukId() + " tidak ditemukan"));

        // Cek stok produk
        if (produk.getStok() < request.getKuantitas()) {
            throw new BadRequestException("Stok tidak mencukupi");
        }

        // Buat item pesanan
        List<PesananItem> items = new ArrayList<>();
        PesananItem pesananItem = new PesananItem();
        pesananItem.setId(UUID.randomUUID().toString());
        pesananItem.setProduk(produk);
        pesananItem.setDeskripsi(produk.getNama());
        pesananItem.setKuantitas(request.getKuantitas());
        pesananItem.setHarga(produk.getHarga());
        pesananItem.setJumlah(produk.getHarga().multiply(BigDecimal.valueOf(request.getKuantitas())));
        pesananItem.setPesanan(pesanan);
        items.add(pesananItem);

        // Set total jumlah pesanan
        BigDecimal jumlah = pesananItem.getJumlah();
        pesanan.setJumlah(jumlah);
        pesanan.setOngkir(request.getOngkir());
        pesanan.setTotal(jumlah.add(pesanan.getOngkir()));

        // Simpan pesanan dan pesanan item ke database
        pesananRepository.save(pesanan);
        pesananItemRepository.save(pesananItem);

        // Update stok produk
        produk.setStok(produk.getStok() - pesananItem.getKuantitas());
        produkRepository.save(produk);

        // Catat log pesanan
        pesananLogService.createLog(username, pesanan, PesananLogService.DRAFT, "Pesanan sukses dibuat");

        // Kembalikan respons pesanan
        return new PesananResponse(pesanan, items);
    }

    // PesananService.java
    @Transactional
    public PesananResponse cancelPesanan(String pesananId, String userId) {
        Pesanan pesanan = pesananRepository.findById(pesananId).orElseThrow(() ->
                new ResourceNotFoundException("Pesanan dengan ID " + pesananId + " tidak ditemukan"));
        if (!userId.equals(pesanan.getPengguna().getId())) {
            throw new BadRequestException("Pesanan hanya dapat dibatalkan oleh pengguna yang bersangkutan");
        }
        if (!StatusPesanan.DRAFT.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestException("Pesanan hanya bisa dibatalkan sebelum pembayaran");
        }
        pesanan.setStatusPesanan(StatusPesanan.DIBATALKAN);
        Pesanan saved = pesananRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.DIBATALKAN, "Pesanan dibatalkan");
        return PesananResponse.builder()
                .id(saved.getId())
                .nomorPesanan(saved.getNomor())
                .tanggal(saved.getTanggal())
                .namaPelanggan(saved.getPengguna().getNama())
                .alamatPengiriman(saved.getAlamatPengiriman())
                .waktuPesan(saved.getWaktuPesan())
                .jumlah(saved.getJumlah())
                .ongkir(saved.getOngkir())
                .total(saved.getTotal())
                .statusPesanan(saved.getStatusPesanan())
                .items(saved.getPesananItems().stream()
                        .map(pesananItem -> {
                            Produk produk = pesananItem.getProduk();
                            return new PesananResponse.Item(produk.getId(), produk.getNama(), pesananItem.getKuantitas(), pesananItem.getHarga(), pesananItem.getJumlah());
                        })
                        .toList())
                .build();
    }

    @Transactional
    public PesananResponse terimaPesanan(String pesananId, String userId){
        Pesanan pesanan = pesananRepository.findById(pesananId).orElseThrow(()->
                new ResourceNotFoundException("Pesanan dengan ID " + pesananId + " tidak ditemukan"));
        if(!userId.equals(pesanan.getPengguna().getId())){
            throw new BadRequestException("Pesanan hanya dapat diterima oleh pengguna yang bersangkutan" + " userId : " + userId + ", getId : " + pesanan.getPengguna().getId());
        }
        if(!StatusPesanan.PENGIRIMAN.equals(pesanan.getStatusPesanan())){
            throw new BadRequestException("Pesanan hanya bisa diterima saat pesanan dalam status PENGIRIMAN, saat ini statusnya adalah : " + pesanan.getStatusPesanan());
        }
        pesanan.setStatusPesanan(StatusPesanan.SELESAI);
        Pesanan saved = pesananRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.SELESAI, "Pesanan diterima");
        return PesananResponse.builder()
                .id(saved.getId())
                .nomorPesanan(saved.getNomor())
                .tanggal(saved.getTanggal())
                .namaPelanggan(saved.getPengguna().getNama())
                .alamatPengiriman(saved.getAlamatPengiriman())
                .waktuPesan(saved.getWaktuPesan())
                .jumlah(saved.getJumlah())
                .ongkir(saved.getOngkir())
                .total(saved.getTotal())
                .statusPesanan(saved.getStatusPesanan())
                .items(saved.getPesananItems().stream()
                        .map(pesananItem -> {
                            Produk produk = pesananItem.getProduk();
                            return new PesananResponse.Item(produk.getId(), produk.getNama(), pesananItem.getKuantitas(), pesananItem.getHarga(), pesananItem.getJumlah());
                        })
                        .toList())
                .build();
    }

    public List<PesananResponse> findAllPesananUser(String userId) {
        List<Pesanan> pesananList = pesananRepository.findByPenggunaId(userId, Sort.by("waktuPesan").descending());

        return pesananList.stream()
                .map(pesanan -> new PesananResponse(pesanan, pesanan.getPesananItems()))
                .toList();
    }

    public List<PesananResponse> findAllPesananPelanggan(String adminId){
        List<Pesanan> pesananList = pesananRepository.findAllPesananPelanggan(adminId);

        return pesananList.stream()
                .map(pesanan -> new PesananResponse(pesanan, pesanan.getPesananItems()))
                .toList();
    }

    public List<Pesanan> search(String filterText, int page, int limit) {
        return pesananRepository.search(filterText.toLowerCase(),
                PageRequest.of(page, limit, Sort.by("waktuPesan").descending()));
    }

    private String generateNomorPesanan() {
        return String.format("%016d", System.nanoTime());
    }

    @Transactional
    public Pesanan konfirmasiPembayaran(String pesananId, String userId) {
        Pesanan pesanan = pesananRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan ID " + pesananId + " tidak ditemukan"));

        if (!StatusPesanan.DRAFT.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestException(
                    "Konfirmasi pesanan gagal, status pesanan saat ini adalah " + pesanan.getStatusPesanan().name());
        }

        pesanan.setStatusPesanan(StatusPesanan.PEMBAYARAN);
        Pesanan saved = pesananRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.PEMBAYARAN, "Pembayaran sukses dikonfirmasi");
        return saved;
    }

    @Transactional
    public Pesanan packing(String pesananId, String userId) {
        Pesanan pesanan = pesananRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan ID " + pesananId + " tidak ditemukan"));

        if (!StatusPesanan.PEMBAYARAN.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestException(
                    "Packing pesanan gagal, status pesanan saat ini adalah " + pesanan.getStatusPesanan().name());
        }

        pesanan.setStatusPesanan(StatusPesanan.PACKING);
        Pesanan saved = pesananRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.PACKING, "Pesanan sedang disiapkan");
        return saved;
    }

    @Transactional
    public Pesanan kirim(String pesananId, String userId) {
        Pesanan pesanan = pesananRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan ID " + pesananId + " tidak ditemukan"));

        if (!StatusPesanan.PACKING.equals(pesanan.getStatusPesanan())) {
            throw new BadRequestException(
                    "Pengiriman pesanan gagal, status pesanan saat ini adalah " + pesanan.getStatusPesanan().name());
        }

        pesanan.setStatusPesanan(StatusPesanan.PENGIRIMAN);
        Pesanan saved = pesananRepository.save(pesanan);
        pesananLogService.createLog(userId, saved, PesananLogService.PENGIRIMAN, "Pesanan sedang dikirim");
        return saved;
    }

    public void delete(String id){
        pesananRepository.deleteById(id);
    }
}
