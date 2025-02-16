package com.khoerulfajri.controller;

import com.khoerulfajri.entity.Kategori;
import com.khoerulfajri.model.PesananRequest;
import com.khoerulfajri.model.PesananResponse;
import com.khoerulfajri.security.service.UserDetailsImpl;
import com.khoerulfajri.service.KategoriService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class KategoriController {

    @Autowired
    private KategoriService kategoriService;

    @GetMapping("/categories")
    public List<Kategori> findAll(){
        return kategoriService.findAll();
    }

    @GetMapping("/categories-admin")
    @PreAuthorize("hasAuthority('admin')")
    public List<Kategori> findAll(@AuthenticationPrincipal UserDetailsImpl admin){
        return kategoriService.findAllByAdmin(admin.getUsername());
    }

    @GetMapping("/categories/{id}")
    public Kategori findById (@PathVariable("id") String id){
        return kategoriService.findById(id);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('admin')")
    public Kategori create(@AuthenticationPrincipal UserDetailsImpl admin, @RequestBody Kategori kategori){
        return kategoriService.create(admin.getUsername(), kategori);
    }

    @PutMapping("/categories")
    public Kategori edit(@RequestBody Kategori kategori){
        return kategoriService.edit(kategori);
    }

    @DeleteMapping("/categories/{id}")
    public void delete(@PathVariable("id") String id){
        kategoriService.deleteById(id);
    }


}
