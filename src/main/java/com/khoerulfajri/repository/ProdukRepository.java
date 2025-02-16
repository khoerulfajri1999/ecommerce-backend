package com.khoerulfajri.repository;

import com.khoerulfajri.entity.Produk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProdukRepository extends JpaRepository<Produk, String>, JpaSpecificationExecutor<Produk> {
    List<Produk> findByPenggunaId(String userId);
}
