package com.khoerulfajri.service;

import com.khoerulfajri.entity.Keranjang;
import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.entity.Produk;
import com.khoerulfajri.exception.BadRequestException;
import com.khoerulfajri.repository.KeranjangRepository;
import com.khoerulfajri.repository.ProdukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class KeranjangService {

    @Autowired
    private ProdukRepository produkRepository;

    @Autowired
    private KeranjangRepository keranjangRepository;

    @Transactional
    public Keranjang addKeranjang(String username, String productId, Integer kuantitas ){

        Produk produk = produkRepository.findById(productId).orElseThrow(() -> new BadRequestException("Produk dengan ID " + productId + " tidak ditemukan"));


        Keranjang keranjang;
        keranjang = new Keranjang();
        keranjang.setKuantitas(kuantitas);
        keranjang.setHarga(produk.getHarga());
        keranjang.setJumlah(BigDecimal.valueOf(produk.getHarga().doubleValue() * keranjang.getKuantitas()));
        keranjang.setPengguna(new Pengguna(username));
        keranjang.setProduk(produk);
        keranjangRepository.save(keranjang);
        
        return keranjang;
    }

    @Transactional
    public Keranjang updateKuantitas(String username, String productId, Integer kuantitas){

        Keranjang keranjang = keranjangRepository.findByPenggunaIdAndProdukId(username, productId).orElseThrow(() ->
                new BadRequestException("Keranjang dengan product ID : " +productId+ " tidak ditemukan"));
        keranjang.setKuantitas(kuantitas);
        keranjang.setJumlah(BigDecimal.valueOf(keranjang.getHarga().doubleValue() * keranjang.getKuantitas()));
        keranjangRepository.save(keranjang);
        return keranjang;
    }

    @Transactional
    public void deleteKeranjang(String username, String productId){
        Keranjang keranjang = keranjangRepository.findByPenggunaIdAndProdukId(username, productId).orElseThrow(() ->
                new BadRequestException("Keranjang dengan product ID : " +productId+ " tidak ditemukan"));
        keranjangRepository.delete(keranjang);
    }

    public List<Keranjang> findByPenggunaId(String username){

        return keranjangRepository.findByPenggunaId(username);
    }

    public Keranjang findById(String username, String id){

        return keranjangRepository.findById(id).orElseThrow(() -> new BadRequestException("Keranjang dengan ID : " +id+ " tidak ditemukan"));
    }

    @Transactional
    public void deleteKeranjangById(String username, String id){
        keranjangRepository.deleteById(id);
    }
}
