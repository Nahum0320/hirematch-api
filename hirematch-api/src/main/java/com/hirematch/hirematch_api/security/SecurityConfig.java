package com.hirematch.hirematch_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/verify-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/resend-code").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hello").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ofertas/publico").permitAll()
                        .requestMatchers(HttpMethod.POST, "/ofertas/buscar").permitAll()
                        .requestMatchers("/api/products").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        // Static resources (HTML admin page)
                        .requestMatchers("/admin-reportes.html").permitAll()
                        .requestMatchers("/*.html", "/*.css", "/*.js", "/images/**").permitAll()
                        // Webhook must come before /api/payments/**
                        .requestMatchers(HttpMethod.POST, "/api/payments/webhook").permitAll()
                        // Authenticated endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/hello-protected").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/profile/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/profile/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/profile").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/profile/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/profile/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/activo").authenticated()
                        .requestMatchers("/api/payments/**").authenticated() // This catches all other /api/payments endpoints
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}