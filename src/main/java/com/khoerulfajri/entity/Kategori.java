package com.khoerulfajri.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;

@Data
@Entity
public class Kategori implements Serializable {

    @Id
    private String id;
    private String nama;
    @JoinColumn
    @ManyToOne
    private Pengguna pengguna;

}
