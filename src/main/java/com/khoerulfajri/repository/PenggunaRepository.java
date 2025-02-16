package com.khoerulfajri.repository;

import com.khoerulfajri.entity.Pengguna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PenggunaRepository extends JpaRepository<Pengguna, String> {
    Boolean existsByEmail(String email);

    Optional<Pengguna> findByNama(String username);
}
