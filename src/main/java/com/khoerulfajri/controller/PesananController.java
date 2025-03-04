package com.khoerulfajri.controller;

import com.khoerulfajri.entity.Pesanan;
import com.khoerulfajri.model.PesananRequest;
import com.khoerulfajri.model.PesananRequestSingle;
import com.khoerulfajri.model.PesananResponse;
import com.khoerulfajri.security.service.UserDetailsImpl;
import com.khoerulfajri.service.PesananService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class PesananController {

    @Autowired
    private PesananService pesananService;

    @PostMapping("/pesanans")
    @PreAuthorize("hasAuthority('user')")
    public PesananResponse create(@AuthenticationPrincipal UserDetailsImpl user, @RequestBody PesananRequest request){
        return pesananService.create(user.getUsername(), request);
    }

    @PostMapping("/pesananssingle")
    @PreAuthorize("hasAuthority('user')")
    public PesananResponse createPesananSingle(@AuthenticationPrincipal UserDetailsImpl user, @RequestBody PesananRequestSingle request){
        return pesananService.createPesananSingle(user.getUsername(), request);
    }

    // PesananController.java
//    @PatchMapping("/pesanans/{pesananId}/cancel")
//    @PreAuthorize("hasAuthority('user')")
//    public PesananResponse cancelPesananUser(
//            @AuthenticationPrincipal UserDetailsImpl user,
//            @PathVariable("pesananId") String pesananId) throws IOException, InterruptedException {
//        return pesananService.cancelPesanan(pesananId, user.getUsername());
//    }

    @PatchMapping("/pesanans/{pesananId}/terima")
    @PreAuthorize("hasAuthority('user')")
    public PesananResponse terima(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("pesananId") String pesananId) {
        return pesananService.terimaPesanan(pesananId, user.getUsername());
    }

    @PatchMapping("/pesanans/{pesananId}/konfirmasi")
    @PreAuthorize("hasAuthority('admin')")
    public Pesanan konfirmasi(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("pesananId") String pesananId) {
        return pesananService.konfirmasiPembayaran(pesananId, user.getUsername());
    }

    @PatchMapping("/pesanans/{pesananId}/packing")
    @PreAuthorize("hasAuthority('admin')")
    public Pesanan packing(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("pesananId") String pesananId) {
        return pesananService.packing(pesananId, user.getUsername());
    }

    @PatchMapping("/pesanans/{pesananId}/kirim")
    @PreAuthorize("hasAuthority('admin')")
    public Pesanan kirim(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("pesananId") String pesananId) {
        return pesananService.kirim(pesananId, user.getUsername());
    }

    @GetMapping("/pesanans")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Map<String, Object>> findAllPesananUser(@AuthenticationPrincipal UserDetailsImpl user) {
        List<PesananResponse> pesananList = pesananService.findAllPesananUser(user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("data", pesananList);
        response.put("totalItems", pesananList.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pesanans/alluser")
    @PreAuthorize("hasAuthority('admin')")
    public List<PesananResponse> findAllPesananPelanggan(@AuthenticationPrincipal UserDetailsImpl user) {
        return pesananService.findAllPesananPelanggan(user.getUsername());
    }


    @GetMapping("/pesanans/admin")
    @PreAuthorize("hasAuthority('admin')")
    public List<Pesanan> search(@AuthenticationPrincipal UserDetailsImpl user,
                                @RequestParam(name = "filterText", defaultValue = "", required = false) String filterText,
                                @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                @RequestParam(name = "limit", defaultValue = "25", required = false) int limit) {
        return pesananService.search(filterText, page, limit);
    }

    @DeleteMapping("/pesanans/{pesananId}")
    @PreAuthorize("hasAuthority('user')")
    public void delete(@PathVariable("pesananId") String pesananId){
        pesananService.delete(pesananId);
    }
}
