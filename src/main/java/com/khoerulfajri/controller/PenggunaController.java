package com.khoerulfajri.controller;

import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.service.PenggunaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class PenggunaController {

    @Autowired
    private PenggunaService penggunaService;

    @GetMapping("/users")
    public List<Pengguna> findAll(){
        return penggunaService.findAll();
    }

    @GetMapping("/users/{id}")
    public Pengguna findById (@PathVariable("id") String id){
        return penggunaService.findById(id);
    }

    @PostMapping("/users")
    public Pengguna create(@RequestBody Pengguna pengguna){
        return penggunaService.create(pengguna);
    }

    @PutMapping("/users")
    public Pengguna edit(@RequestBody Pengguna pengguna){
        return penggunaService.edit(pengguna);
    }

    @PutMapping("/profile")
    public Pengguna updateProfile(@RequestBody Pengguna pengguna){
        Pengguna old = penggunaService.findById(pengguna.getId());
        pengguna.setPassword(old.getPassword());
        return penggunaService.edit(pengguna);
    }

    @DeleteMapping("/users/{id}")
    public void delete(@PathVariable("id") String id){
        penggunaService.deleteById(id);
    }


}
