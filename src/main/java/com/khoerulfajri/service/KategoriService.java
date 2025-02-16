package com.khoerulfajri.service;

import com.khoerulfajri.entity.Kategori;
import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.exception.ResourceNotFoundException;
import com.khoerulfajri.repository.KategoriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KategoriService {

    @Autowired
    private KategoriRepository kategoriRepository;

    public Kategori findById(String id){
        return kategoriRepository
                .findById(id).orElseThrow(() -> new ResourceNotFoundException("Kategori dengan id " + id + " tidak ditemukan!!"));
    }

    public List<Kategori> findAll(){
        return kategoriRepository.findAll();
    }

    public List<Kategori> findAllByAdmin(String userId){
        List<Kategori> kategoriList = kategoriRepository.findByPenggunaId(userId);
        return kategoriList;
    }

    public Kategori create(String username, Kategori kategori){
        kategori.setPengguna(new Pengguna(username));
        kategori.setId(UUID.randomUUID().toString());
        return kategoriRepository.save(kategori);
    }

    public Kategori edit(Kategori kategori){
        return kategoriRepository.save(kategori);
    }

    public void deleteById(String id){
        kategoriRepository.deleteById(id);
    }

}
