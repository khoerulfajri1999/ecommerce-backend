package com.khoerulfajri.service;

import com.khoerulfajri.entity.*;
import com.khoerulfajri.exception.BadRequestException;
import com.khoerulfajri.exception.ResourceNotFoundException;
import com.khoerulfajri.model.*;
import com.khoerulfajri.repository.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

@Service
@RequiredArgsConstructor
public class PesananService implements Serializable {

    private final ProdukRepository produkRepository;
    private final PesananRepository pesananRepository;
    private final PenggunaRepository penggunaRepository;
    private final PesananItemRepository pesananItemRepository;
    private final KeranjangService keranjangService;
    private final PesananLogService pesananLogService;
    private final RestClient restClient;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final WalletRepository walletRepository;
    private final WalletService walletService;

    @Value("${payment.secret-key}")
    private String MIDTRANS_SERVER_KEY;

    @Value("${payment.url}")
    private String MIDTRANS_API_URL;

    @Value("${payment.url2}")
    private String MIDTRANS_API_URL2;

    @Transactional(rollbackFor = Exception.class)
    public PesananResponse create(String username, PesananRequest request) {
        Pesanan pesanan = Pesanan.builder()
                .id(UUID.randomUUID().toString())
                .tanggal(Date.from(ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toInstant())) // Tanggal dalam UTC+7
                .nomor(generateNomorPesanan())
                .pengguna(new Pengguna(username))
                .alamatPengiriman(request.getAlamatPengiriman())
                .statusPesanan(StatusPesanan.DRAFT)
                .waktuPesan(Date.from(ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).toInstant())) // Waktu pesan dalam UTC+7
                .build();

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

        BigDecimal jumlah = items.stream()
                .map(PesananItem::getJumlah)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pesanan = Pesanan.builder()
                .id(pesanan.getId())
                .tanggal(pesanan.getTanggal())
                .nomor(pesanan.getNomor())
                .pengguna(pesanan.getPengguna())
                .alamatPengiriman(pesanan.getAlamatPengiriman())
                .statusPesanan(pesanan.getStatusPesanan())
                .waktuPesan(pesanan.getWaktuPesan())
                .jumlah(jumlah)
                .ongkir(request.getOngkir())
                .total(jumlah.add(request.getOngkir()))
                .build();

        Pesanan saved = pesananRepository.save(pesanan);
        for (PesananItem pesananItem : items) {
            pesananItemRepository.save(pesananItem);
            Produk produk = pesananItem.getProduk();
            produk.setStok(produk.getStok() - pesananItem.getKuantitas());
            produkRepository.save(produk);
            keranjangService.deleteKeranjang(username, produk.getId());
        }

        pesananLogService.createLog(username, pesanan, PesananLogService.DRAFT, "Pesanan sukses dibuat");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentDetail(
                        PaymentDetailRequest.builder()
                                .orderId(saved.getId())
                                .grossAmount(saved.getTotal().longValue())
                                .build()
                )
                .penggunaRequest(
                        PenggunaRequest.builder()
                                .id(saved.getPengguna().getId())
                                .nama(saved.getPengguna().getNama())
                                .alamat(saved.getPengguna().getAlamat())
                                .gambar(saved.getPengguna().getGambar())
                                .email(saved.getPengguna().getEmail())
                                .hp(saved.getPengguna().getHp())
                                .build()
                )
                .build();
        ResponseEntity<Map<String, String>> response = restClient.post()
                .uri(MIDTRANS_API_URL)
                .body(paymentRequest)
                .header("Authorization", "Basic " + MIDTRANS_SERVER_KEY)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
        Map<String, String> body = response.getBody();
        saved = Pesanan.builder()
                .id(saved.getId())
                .nomor(saved.getNomor())
                .tanggal(saved.getTanggal())
                .pengguna(saved.getPengguna())
                .alamatPengiriman(saved.getAlamatPengiriman())
                .jumlah(saved.getJumlah())
                .ongkir(saved.getOngkir())
                .total(saved.getTotal())
                .statusPesanan(saved.getStatusPesanan())
                .waktuPesan(saved.getWaktuPesan())
                .token(body.get("token"))
                .redirectUrl(body.get("redirect_url"))
                .vaNumber(body.get("va_number"))
                .build();
        pesananRepository.save(saved);

        return new PesananResponse(saved, items);
    }

//    @Transactional(rollbackFor = Exception.class)
//    public PesananResponse create(String username, PesananRequest request) {
//        Map<String, Pesanan> pesananMap = new HashMap<>();
//        Map<String, List<PesananItem>> itemsMap = new HashMap<>();
//
//        for (KeranjangRequest k : request.getItems()) {
//            Produk produk = produkRepository.findById(k.getProdukId())
//                    .orElseThrow(() -> new BadRequestException("Produk ID " + k.getProdukId() + " tidak ditemukan"));
//            if (produk.getStok() < k.getKuantitas()) {
//                throw new BadRequestException("Stok tidak mencukupi");
//            }
//
//            String namaPenjual = produk.getPengguna().getNama();
//            pesananMap.putIfAbsent(namaPenjual, Pesanan.builder()
//                    .id(UUID.randomUUID().toString())
//                    .tanggal(new Date())
//                    .nomor(generateNomorPesanan())
//                    .pengguna(new Pengguna(username))
//                    .alamatPengiriman(request.getAlamatPengiriman())
//                    .statusPesanan(StatusPesanan.DRAFT)
//                    .waktuPesan(new Date())
//                    .build());
//
//            PesananItem pi = new PesananItem();
//            pi.setId(UUID.randomUUID().toString());
//            pi.setProduk(produk);
//            pi.setDeskripsi(produk.getNama());
//            pi.setKuantitas(k.getKuantitas());
//            pi.setHarga(produk.getHarga());
//            pi.setJumlah(new BigDecimal(pi.getHarga().doubleValue() * pi.getKuantitas()));
//            pi.setPesanan(pesananMap.get(namaPenjual));
//
//            itemsMap.computeIfAbsent(namaPenjual, key -> new ArrayList<>()).add(pi);
//        }
//
//        List<PesananResponse> responses = new ArrayList<>();
//
//        for (Map.Entry<String, Pesanan> entry : pesananMap.entrySet()) {
//            Pesanan pesanan = entry.getValue();
//            List<PesananItem> items = itemsMap.get(entry.getKey());
//
//            BigDecimal jumlah = items.stream()
//                    .map(PesananItem::getJumlah)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            pesanan.setJumlah(jumlah);
//            pesanan.setOngkir(request.getOngkir());
//            pesanan.setTotal(jumlah.add(request.getOngkir()));
//
//            Pesanan saved = pesananRepository.save(pesanan);
//            for (PesananItem pesananItem : items) {
//                pesananItemRepository.save(pesananItem);
//                Produk produk = pesananItem.getProduk();
//                produk.setStok(produk.getStok() - pesananItem.getKuantitas());
//                produkRepository.save(produk);
//                keranjangService.deleteKeranjang(username, produk.getId());
//            }
//
//            pesananLogService.createLog(username, pesanan, PesananLogService.DRAFT, "Pesanan sukses dibuat");
//
//            PaymentRequest paymentRequest = PaymentRequest.builder()
//                    .paymentDetail(PaymentDetailRequest.builder()
//                            .orderId(saved.getId())
//                            .grossAmount(saved.getTotal().longValue())
//                            .build())
//                    .penggunaRequest(PenggunaRequest.builder()
//                            .id(saved.getPengguna().getId())
//                            .nama(saved.getPengguna().getNama())
//                            .alamat(saved.getPengguna().getAlamat())
//                            .gambar(saved.getPengguna().getGambar())
//                            .email(saved.getPengguna().getEmail())
//                            .hp(saved.getPengguna().getHp())
//                            .build())
//                    .build();
//
//            ResponseEntity<Map<String, String>> response = restClient.post()
//                    .uri(MIDTRANS_API_URL)
//                    .body(paymentRequest)
//                    .header("Authorization", "Basic " + MIDTRANS_SERVER_KEY)
//                    .retrieve()
//                    .toEntity(new ParameterizedTypeReference<>() {});
//            Map<String, String> body = response.getBody();
//
//            saved.setToken(body.get("token"));
//            saved.setRedirectUrl(body.get("redirect_url"));
//            saved.setVaNumber(body.get("va_number"));
//            pesananRepository.save(saved);
//
//            responses.add(new PesananResponse(saved, items));
//        }
//
//        return responses;
//    }



    @Transactional(rollbackFor = Exception.class)
    public String getPesananStatus(String orderId) throws IOException, InterruptedException {
        Pesanan pesanan = pesananRepository.findById(orderId).get();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MIDTRANS_API_URL2 + orderId + "/status"))
                .header("accept", "application/json")
                .header("Authorization", "Basic " + MIDTRANS_SERVER_KEY)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject jsonObject = new JSONObject(response.body());

        if (parseInt(jsonObject.getString("status_code")) != 200) {
            throw new BadRequestException("Transaksi gagal");
        }

        if (!StatusPesanan.isDraftOrSettlement(jsonObject.getString("transaction_status"))) {
            throw new BadRequestException("Transaksi sudah dilakukan");
        }

        if (pesanan.getToken() == null) {
            throw new BadRequestException("Transaksi sudah dilakukan");
        }

        Wallet wallet = walletRepository.findByPenggunaId(pesananRepository.findById(orderId).get().getPengguna().getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal grossAmount = new BigDecimal(jsonObject.getString("gross_amount"));
        if (wallet.getBalance() < grossAmount.longValue()) {
            throw new BadRequestException("Saldo tidak mencukupi");
        }
        WalletRequest requestWallet = WalletRequest.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance() - grossAmount.longValue())
                .build();
        walletService.updateWallet(requestWallet);

        Pesanan saved = Pesanan.builder()
                .id(pesanan.getId())
                .nomor(pesanan.getNomor())
                .tanggal(pesanan.getTanggal())
                .pengguna(pesanan.getPengguna())
                .alamatPengiriman(pesanan.getAlamatPengiriman())
                .jumlah(pesanan.getJumlah())
                .ongkir(pesanan.getOngkir())
                .total(pesanan.getTotal())
                .statusPesanan(StatusPesanan.PEMBAYARAN)
                .waktuPesan(pesanan.getWaktuPesan())
                .build();
        pesananRepository.save(saved);
        return response.body();
    }

    @Transactional
    public PesananResponse createPesananSingle(String username, PesananRequestSingle request) {
        Produk produk = produkRepository.findById(request.getProdukId())
                .orElseThrow(() -> new BadRequestException("Produk ID " + request.getProdukId() + " tidak ditemukan"));

        if (produk.getStok() < request.getKuantitas()) {
            throw new BadRequestException("Stok tidak mencukupi");
        }

        BigDecimal jumlah = produk.getHarga().multiply(BigDecimal.valueOf(request.getKuantitas()));

        Pesanan pesanan = Pesanan.builder()
                .id(UUID.randomUUID().toString())
                .tanggal(new Date())
                .nomor(generateNomorPesanan())
                .pengguna(new Pengguna(username))
                .alamatPengiriman(request.getAlamatPengiriman())
                .statusPesanan(StatusPesanan.DRAFT)
                .waktuPesan(new Date())
                .jumlah(jumlah)
                .ongkir(request.getOngkir())
                .total(jumlah.add(request.getOngkir()))
                .build();

        List<PesananItem> items = new ArrayList<>();
        PesananItem pesananItem = new PesananItem();
        pesananItem.setId(UUID.randomUUID().toString());
        pesananItem.setProduk(produk);
        pesananItem.setDeskripsi(produk.getNama());
        pesananItem.setKuantitas(request.getKuantitas());
        pesananItem.setHarga(produk.getHarga());
        pesananItem.setJumlah(jumlah);
        pesananItem.setPesanan(pesanan);
        items.add(pesananItem);

        // Simpan pesanan dan pesanan item ke database
       Pesanan saved = pesananRepository.save(pesanan);
        pesananItemRepository.save(pesananItem);

        // Update stok produk
        produk.setStok(produk.getStok() - pesananItem.getKuantitas());
        produkRepository.save(produk);

        // Catat log pesanan
        pesananLogService.createLog(username, pesanan, PesananLogService.DRAFT, "Pesanan sukses dibuat");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentDetail(
                        PaymentDetailRequest.builder()
                                .orderId(saved.getId())
                                .grossAmount(saved.getTotal().longValue())
                                .build()
                )
                .penggunaRequest(
                        PenggunaRequest.builder()
                                .id(saved.getPengguna().getId())
                                .nama(saved.getPengguna().getNama())
                                .alamat(saved.getPengguna().getAlamat())
                                .gambar(saved.getPengguna().getGambar())
                                .email(saved.getPengguna().getEmail())
                                .hp(saved.getPengguna().getHp())
                                .build()
                )
                .build();
        ResponseEntity<Map<String, String>> response = restClient.post()
                .uri(MIDTRANS_API_URL)
                .body(paymentRequest)
                .header("Authorization", "Basic " + MIDTRANS_SERVER_KEY)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
        Map<String, String> body = response.getBody();
        saved = Pesanan.builder()
                .id(saved.getId())
                .nomor(saved.getNomor())
                .tanggal(saved.getTanggal())
                .pengguna(saved.getPengguna())
                .alamatPengiriman(saved.getAlamatPengiriman())
                .jumlah(saved.getJumlah())
                .ongkir(saved.getOngkir())
                .total(saved.getTotal())
                .statusPesanan(saved.getStatusPesanan())
                .waktuPesan(saved.getWaktuPesan())
                .token(body.get("token"))
                .redirectUrl(body.get("redirect_url"))
                .vaNumber(body.get("va_number"))
                .build();
        pesananRepository.save(saved);

        // Kembalikan respons pesanan
        return new PesananResponse(saved, items);
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
                            return new PesananResponse.Item(produk.getId(), produk, produk.getNama(), pesananItem.getKuantitas(), pesananItem.getHarga(), pesananItem.getJumlah());
                        })
                        .toList())
                .build();
    }

//    @Transactional
//    public PesananResponse cancelPesanan(String pesananId) throws IOException, InterruptedException {
//        Pesanan pesanan = pesananRepository.findById(pesananId).orElseThrow(() ->
//                new ResourceNotFoundException("Pesanan dengan ID " + pesananId + " tidak ditemukan"));
//
//        String refundUrl = MIDTRANS_API_URL2 + pesananId + "/refund";
//        JSONObject refundRequest = new JSONObject();
//        refundRequest.put("reason", "Customer requested refund");
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(refundUrl))
//                .header("accept", "application/json")
//                .header("Authorization", "Basic " + MIDTRANS_SERVER_KEY)
//                .POST(HttpRequest.BodyPublishers.ofString(refundRequest.toString()))
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        JSONObject jsonObject = new JSONObject(response.body());
////        if (!userId.equals(pesanan.getPengguna().getId())) {
////            throw new BadRequestException("Pesanan hanya dapat dibatalkan oleh pengguna yang bersangkutan");
////        }
//        System.out.println(jsonObject);
//        if (!StatusPesanan.DRAFT.equals(pesanan.getStatusPesanan())) {
//            throw new BadRequestException("Pesanan hanya bisa dibatalkan sebelum pembayaran");
//        }
//
//        if (parseInt(jsonObject.getString("status_code")) != 200) {
//            throw new BadRequestException("Refund gagal");
//        }
//
//        if (!StatusPesanan.isDibatalkanOrRefund(jsonObject.getString("transaction_status"))) {
//            throw new BadRequestException("Refund sudah dilakukan");
//        }
//
//        if (pesanan.getToken() == null) {
//            throw new BadRequestException("Transaksi sudah dilakukan");
//        }
//
//        Wallet wallet = walletRepository.findByPenggunaId(pesananRepository.findById(pesananId).get().getPengguna().getId())
//                .orElseThrow(() -> new RuntimeException("Wallet not found"));
//
//        BigDecimal grossAmount = new BigDecimal(jsonObject.getString("gross_amount"));
//        WalletRequest requestWallet = WalletRequest.builder()
//                .id(wallet.getId())
//                .balance(wallet.getBalance() + grossAmount.longValue())
//                .build();
//        walletService.updateWallet(requestWallet);
//
//        pesanan.setStatusPesanan(StatusPesanan.DIBATALKAN);
//        Pesanan saved = pesananRepository.save(pesanan);
//        pesananLogService.createLog(pesanan.getPengguna().getId(), saved, PesananLogService.DIBATALKAN, "Pesanan dibatalkan");
//        return PesananResponse.builder()
//                .id(saved.getId())
//                .nomorPesanan(saved.getNomor())
//                .tanggal(saved.getTanggal())
//                .namaPelanggan(saved.getPengguna().getNama())
//                .alamatPengiriman(saved.getAlamatPengiriman())
//                .waktuPesan(saved.getWaktuPesan())
//                .jumlah(saved.getJumlah())
//                .ongkir(saved.getOngkir())
//                .total(saved.getTotal())
//                .statusPesanan(saved.getStatusPesanan())
//                .items(saved.getPesananItems().stream()
//                        .map(pesananItem -> {
//                            Produk produk = pesananItem.getProduk();
//                            return new PesananResponse.Item(produk.getId(), produk, produk.getNama(), pesananItem.getKuantitas(), pesananItem.getHarga(), pesananItem.getJumlah());
//                        })
//                        .toList())
//                .build();
//    }

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

        PesananResponse pesananResponse = PesananResponse.builder()
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
                            return new PesananResponse.Item(produk.getId(), produk, produk.getNama(), pesananItem.getKuantitas(), pesananItem.getHarga(), pesananItem.getJumlah());
                        })
                        .toList())
                .build();

        String Id = pesananResponse.getItems().get(0).getProdukId();
        System.out.println(Id);
        Produk produk = produkRepository.findById(Id).orElseThrow(() ->
                new ResourceNotFoundException("Produk dengan ID " + Id + " tidak ditemukan"));
        System.out.println(produk);
        System.out.println(produk.getPengguna().getId());
        Wallet wallet = walletRepository.findByPenggunaId(produk.getPengguna().getId()).orElseThrow(() ->
                new ResourceNotFoundException("Wallet tidak ditemukan"));

        WalletRequest requestWallet = WalletRequest.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance() + pesananResponse.getTotal().longValue())
                .build();
        walletService.updateWallet(requestWallet);

        return pesananResponse;
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

    public boolean existsByPesananId(String orderId) {
        return pesananRepository.existsById(orderId);
    }
}
