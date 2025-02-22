package com.khoerulfajri.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class Pengguna implements Serializable {

    @Id
    private String id;
    private String nama;
    @JsonIgnore
    private String password;
    private String email;
    private String alamat;
    private String gambar;
    private String hp;
    private String roles;
    private Boolean isAktif;

    public Pengguna(String username) {
        this.id = username;
    }
}
