package com.khoerulfajri.service;

import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.exception.BadRequestException;
import com.khoerulfajri.exception.ResourceNotFoundException;
import com.khoerulfajri.repository.PenggunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class PenggunaService {

    @Autowired
    private PenggunaRepository penggunaRepository;

    public Pengguna findById(String id){
        return penggunaRepository
                .findById(id).orElseThrow(() -> new ResourceNotFoundException("Pengguna dengan id " + id + " tidak ditemukan!!"));
    }

    public List<Pengguna> findAll(){
        return penggunaRepository.findAll();
    }

    public Pengguna create(Pengguna pengguna){

        if(!StringUtils.hasText(pengguna.getId())){
            throw new BadRequestException("ID pengguna tidak boleh kosong");
        }

        if(penggunaRepository.existsById(pengguna.getId())){
            throw new BadRequestException("Username " + pengguna.getId() + " sudah terdaftar");
        }

        if(!StringUtils.hasText(pengguna.getEmail())){
            throw new BadRequestException("Email pengguna tidak boleh kosong");
        }

        if(penggunaRepository.existsByEmail(pengguna.getEmail())){
            throw new BadRequestException("Email " + pengguna.getEmail() + " sudah terdaftar");
        }

        pengguna.setIsAktif(true);
        return penggunaRepository.save(pengguna);
    }

    public Pengguna edit(Pengguna pengguna){

        if(!StringUtils.hasText(pengguna.getId())){
            throw new BadRequestException("ID pengguna tidak boleh kosong");
        }

        if(!StringUtils.hasText(pengguna.getEmail())){
            throw new BadRequestException("Email pengguna tidak boleh kosong");
        }

        return penggunaRepository.save(pengguna);
    }

    public void deleteById(String id){
        penggunaRepository.deleteById(id);
    }

}
