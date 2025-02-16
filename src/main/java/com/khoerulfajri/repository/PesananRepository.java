package com.khoerulfajri.repository;

import com.khoerulfajri.entity.Pesanan;
import com.khoerulfajri.model.PesananResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PesananRepository extends JpaRepository<Pesanan, String> {
    List<PesananResponse> findByPenggunaId(String userId, Pageable pageable);
    List<Pesanan> findByPenggunaId(String userId, Sort sort);

    @Query("SELECT p FROM Pesanan p WHERE LOWER(p.nomor) LIKE %:filterText% OR LOWER(p.pengguna.nama) LIKE %:filterText%")
    List<Pesanan> search(@Param("filterText") String filterText, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Pesanan p " +
            "JOIN p.pesananItems pi " +
            "JOIN pi.produk pr " +
            "WHERE pr.pengguna.id = :adminId")
    List<Pesanan> findAllPesananPelanggan(@Param("adminId") String adminId);
}
