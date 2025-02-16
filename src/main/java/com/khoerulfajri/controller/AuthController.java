package com.khoerulfajri.controller;

import com.khoerulfajri.model.*;
import com.khoerulfajri.security.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khoerulfajri.entity.Pengguna;
import com.khoerulfajri.security.jwt.JwtUtils;
import com.khoerulfajri.security.service.UserDetailsImpl;
import com.khoerulfajri.service.PenggunaService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PenggunaService penggunaService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailServiceImpl userDetailServiceImpl;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<CommonResponse<JwtResponse>> authenticateUser(@RequestBody LoginRequest request) {
        try {
            // Autentikasi menggunakan username dan password
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            // Set authentication di SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token dan refresh token
            String token = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshJwtToken(authentication);

            // Ambil informasi pengguna
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();

            // Buat JwtResponse untuk dikirimkan dalam response body
            JwtResponse jwtResponse = new JwtResponse(token, refreshToken, principal.getUsername(), principal.getEmail(), principal.getRoles(), principal.getNama());

            // Response dengan CommonResponse
            CommonResponse<JwtResponse> response = CommonResponse.<JwtResponse>builder()
                    .statusCode(HttpStatus.OK.value())  // Status HTTP 200 OK
                    .message("Login berhasil")          // Pesan sukses
                    .data(jwtResponse)                  // Data yang dikembalikan (JWT)
                    .build();

            return ResponseEntity.ok(response);  // Mengembalikan response dengan status OK
        } catch (Exception e) {
            // Menangani kesalahan jika terjadi
            CommonResponse<JwtResponse> errorResponse = CommonResponse.<JwtResponse>builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())  // Status HTTP 401 Unauthorized
                    .message("Username atau password salah.")      // Pesan error
                    .data(null)  // Tidak ada data yang dikirim jika gagal login
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);  // Mengembalikan response dengan status Unauthorized
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Pengguna>> signup(@RequestBody SignUpRequest request) {

        Pengguna pengguna = new Pengguna();
        pengguna.setId(request.getUsername());
        pengguna.setEmail(request.getEmail());
        pengguna.setPassword(passwordEncoder.encode(request.getPassword()));
        pengguna.setNama(request.getNama());
        pengguna.setRoles("user");
        Pengguna created = penggunaService.create(pengguna);
        CommonResponse<Pengguna> response = CommonResponse.<Pengguna>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Pengguna baru ditambahkan")
                .data(pengguna)
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body(response);
    }

    @PostMapping("/signup-admin")
    public ResponseEntity<CommonResponse<Pengguna>> signupAdmin(@RequestBody SignUpRequest request) {

        Pengguna pengguna = new Pengguna();
        pengguna.setId(request.getUsername());
        pengguna.setEmail(request.getEmail());
        pengguna.setPassword(passwordEncoder.encode(request.getPassword()));
        pengguna.setNama(request.getNama());
        pengguna.setRoles("admin");
        Pengguna created = penggunaService.create(pengguna);
        CommonResponse<Pengguna> response = CommonResponse.<Pengguna>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Admin baru ditambahkan")
                .data(pengguna)
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body(response);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        boolean valid = jwtUtils.validateJwtToken(token);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String username = jwtUtils.getUserNameFromJwtToken(token);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetailServiceImpl.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetailsImpl, null,
                userDetailsImpl.getAuthorities());
        String newToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshJwtToken(authentication);
        return ResponseEntity.ok(new JwtResponse(newToken, refreshToken, username, userDetailsImpl.getEmail(), userDetailsImpl.getRoles(), userDetailsImpl.getNama()));
    }
}