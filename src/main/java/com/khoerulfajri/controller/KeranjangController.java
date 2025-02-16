package com.khoerulfajri.controller;

import com.khoerulfajri.entity.Keranjang;
import com.khoerulfajri.model.KeranjangRequest;
import com.khoerulfajri.security.service.UserDetailsImpl;
import com.khoerulfajri.service.KeranjangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class KeranjangController {

    @Autowired
    private KeranjangService keranjangService;

    @GetMapping("/keranjangs")
    public List<Keranjang> findByPenggunaId(@AuthenticationPrincipal UserDetailsImpl user){
        return keranjangService.findByPenggunaId(user.getUsername());
    }

    @GetMapping("/keranjangs/{id}")
    public Keranjang findById(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable("id") String id){
        return keranjangService.findById(user.getUsername(), id);
    }

    @PostMapping("/keranjangs")
    public Keranjang create(@AuthenticationPrincipal UserDetailsImpl user, @RequestBody KeranjangRequest request){
        return keranjangService.addKeranjang(user.getUsername(), request.getProdukId(), request.getKuantitas());
    }

    @PatchMapping("keranjangs/{productId}")
    public Keranjang update(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable("productId") String productId, @RequestParam("kuantitas") Integer kuantitas){
        return keranjangService.updateKuantitas(user.getUsername(), productId, kuantitas);
    }

    @DeleteMapping("keranjangs/product/{productId}")
    public void delete(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable("productId") String productId){
        keranjangService.deleteKeranjang(user.getUsername(), productId);
    }

    @DeleteMapping("keranjangs/{id}")
    public void deleteById(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable("id") String id){
        keranjangService.deleteKeranjangById(user.getUsername(), id);
    }
}
