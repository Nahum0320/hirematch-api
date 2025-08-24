package com.hirematch.hirematch_api.security;

import com.hirematch.hirematch_api.repository.UsuarioRepository;
import com.hirematch.hirematch_api.service.SesionService;
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
    private final SesionService sesionService;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository, SesionService sesionService) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.sesionService = sesionService;
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
                    Long numeroSesion = Long.parseLong(subject);
                    
                    // Verificar que la sesión esté activa
                    if (sesionService.obtenerSesion(numeroSesion).isActiva()) {
                        var sesion = sesionService.obtenerSesion(numeroSesion);
                        
                        if (sesion != null && sesion.getUsuario() != null) {
                            var usuario = sesion.getUsuario();
                            var authentication = new UsernamePasswordAuthenticationToken(
                                    usuario, null, usuario.getAuthorities()
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                logger.warn("ID de sesión inválido en token: " + e.getMessage());
            } catch (Exception e) {
                logger.warn("Token inválido: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
