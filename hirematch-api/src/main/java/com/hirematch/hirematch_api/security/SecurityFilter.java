package com.hirematch.hirematch_api.security;

import com.hirematch.hirematch_api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            
            try {
                String subject = tokenService.getSubject(token);

                if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var usuario = usuarioRepository.findByEmail(subject)
                            .orElse(null);

                    if (usuario != null) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                usuario, null, usuario.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // Log del error sin exponer detalles
                logger.warn("Token inválido: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
