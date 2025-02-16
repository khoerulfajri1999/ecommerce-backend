package com.khoerulfajri.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtResponse implements Serializable {

    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;
    private String role;
    private String nama;

    public JwtResponse(String accessToken, String refreshToken, String username, String email, String role, String nama) {
        this.token = accessToken;
        this.username = username;
        this.email = email;
        this.refreshToken = refreshToken;
        this.role = role;
        this.nama = nama;
    }
}
