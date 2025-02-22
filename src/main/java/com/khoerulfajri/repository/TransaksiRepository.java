package com.khoerulfajri.repository;

import com.khoerulfajri.entity.Transaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, String> {

}
