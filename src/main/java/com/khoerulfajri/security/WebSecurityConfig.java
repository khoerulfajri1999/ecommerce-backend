package com.khoerulfajri.security;

import com.khoerulfajri.security.jwt.AuthEntryPointJwt;
import com.khoerulfajri.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

//    @Bean
//    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.cors(cors -> cors.disable())
//                .csrf(csrf -> csrf.disable())
//                .exceptionHandling(exceptionHandling ->
//                        exceptionHandling.authenticationEntryPoint(unauthorizedHandler)
//                )
//                .sessionManagement(sessionManagement ->
//                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authorizeHttpRequests(authorize ->
//                        authorize
//                                .requestMatchers("/auth/**").permitAll()
//                                .requestMatchers("/api/**").permitAll()
////                                .requestMatchers("/api/payment/midtrans-webhook").permitAll()
//                                .requestMatchers("/**").permitAll()
//
//                                .anyRequest().authenticated()
//                )
//                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//            return http.build();
//    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(unauthorizedHandler)
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/api/payment/notification").permitAll() // Pastikan ini ada!
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authentication) throws Exception {
        return authentication.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("https://api.sandbox.midtrans.com");
        config.addAllowedOriginPattern("https://*.ngrok-free.app");
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}