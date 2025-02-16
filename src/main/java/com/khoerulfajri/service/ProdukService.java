package com.khoerulfajri.service;

import com.khoerulfajri.entity.Kategori;
import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.entity.Produk;
import com.khoerulfajri.exception.BadRequestException;
import com.khoerulfajri.exception.ResourceNotFoundException;
import com.khoerulfajri.model.ProdukResponse;
import com.khoerulfajri.model.SearchRequest;
import com.khoerulfajri.repository.KategoriRepository;
import com.khoerulfajri.repository.PenggunaRepository;
import com.khoerulfajri.repository.ProdukRepository;
import com.khoerulfajri.security.service.UserDetailsImpl;
import com.khoerulfajri.util.specification.ProdukSpesifikasi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class ProdukService {

    @Autowired
    private KategoriRepository kategoriRepository;

    @Autowired
    private ProdukRepository produkRepository;

    @Autowired
    private PenggunaRepository penggunaRepository;

    public Produk findByid(String id){
        return produkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produk dengan id " + id + " tidak ditemukan!!"));
    }

    public List<Produk> findAll(){
        return produkRepository.findAll();
    }

    public List<Produk> findAllByAdmin(String userId){
        List<Produk> produkList = produkRepository.findByPenggunaId(userId);
        return produkList;
    }

    public Page<Produk> findAllSearch(SearchRequest searchRequest) {

        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Specification<Produk> specification = ProdukSpesifikasi.getSpecification(searchRequest.getQuery());

        return produkRepository.findAll(specification, pageable);
    }

    public ProdukResponse create(Produk produk) {

        // Validasi Nama Produk
        if (!StringUtils.hasText(produk.getNama())) {
            throw new BadRequestException("Nama tidak boleh kosong");
        }

        // Validasi Kategori Produk
        if (produk.getKategori() == null) {
            throw new BadRequestException("Kategori tidak boleh kosong");
        }

        if (!StringUtils.hasText(produk.getKategori().getId())) {
            throw new BadRequestException("ID kategori tidak boleh kosong");
        }

        // Cek apakah kategori ada di database
        Kategori kategori = kategoriRepository.findById(produk.getKategori().getId())
                .orElseThrow(() -> new BadRequestException(
                        "Kategori dengan ID " + produk.getKategori().getId() + " tidak ada dalam database"
                ));

        // Generate UUID untuk produk
        produk.setId(UUID.randomUUID().toString());

        // Mendapatkan pengguna yang sedang login dari SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            throw new BadRequestException("Pengguna tidak valid atau tidak terautentikasi");
        }

        String id = ((UserDetailsImpl) auth.getPrincipal()).getUsername();
        Pengguna pengguna = penggunaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pengguna tidak ditemukan"));

        // Set pengguna pada produk
        produk.setPengguna(pengguna);
        produk.setKategori(kategori); // Pastikan kategori juga sudah diset

        // Simpan ke database
        produkRepository.save(produk);

        // Return response menggunakan ProdukResponse DTO
        return ProdukResponse.builder()
                .id(produk.getId())
                .nama(produk.getNama())
                .deskripsi(produk.getDeskripsi())
                .gambar(produk.getGambar())
                .harga(produk.getHarga())
                .stok(produk.getStok())
                .kategori(produk.getKategori()) // Pastikan kategori ini hanya mengembalikan data yang diperlukan
                .pengguna(pengguna) // Jika `pengguna` adalah objek, simpan hanya username
                .build();
    }
    public Produk edit(Produk produk){

        if(!StringUtils.hasText(produk.getId())){
            throw new BadRequestException("ID tidak boleh kosong");
        }

        produkRepository.findById(produk.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Produk dengan ID " + produk.getId() + " tidak ditemukan"
                ));

        if(!StringUtils.hasText(produk.getNama())){
            throw new BadRequestException("Nama tidak boleh kosong");
        }

        if(produk.getKategori() == null){
            throw new BadRequestException("Kategori tidak boleh kosong");
        }

        if(!StringUtils.hasText(produk.getKategori().getId())){
            throw new BadRequestException("ID kategori tidak boleh kosong");
        }

        kategoriRepository.findById(produk.getKategori().getId())
                .orElseThrow(() -> new BadRequestException(
                        "Kategori dengan ID " + produk.getKategori().getId() + " tidak ada dalam database"
                ));

        return produkRepository.save(produk);
    }

    public Produk ubahGambar(String id, String gambar){
        Produk produk = findByid(id);
        produk.setGambar(gambar);
        return produkRepository.save(produk);
    }

    public void delete(String id){
        produkRepository.deleteById(id);
    }

}
