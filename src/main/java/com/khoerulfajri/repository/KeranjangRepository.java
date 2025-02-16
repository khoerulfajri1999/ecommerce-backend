package com.khoerulfajri.repository;

import com.khoerulfajri.entity.Keranjang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeranjangRepository extends JpaRepository<Keranjang, String> {

    Optional<Keranjang> findByPenggunaIdAndProdukId (String username, String productId);
    List<Keranjang> findByPenggunaId(String username);

}
