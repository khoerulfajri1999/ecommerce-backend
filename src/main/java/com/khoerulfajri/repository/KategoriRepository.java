package com.khoerulfajri.repository;

import com.khoerulfajri.entity.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KategoriRepository extends JpaRepository<Kategori, String> {
    List<Kategori> findByPenggunaId(String userId);
}
