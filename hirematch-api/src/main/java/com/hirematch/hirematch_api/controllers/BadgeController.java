package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.*;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.BadgeService;
import com.hirematch.hirematch_api.service.EstadisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private EstadisticaService estadisticaService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @GetMapping("/usuario")
    public ResponseEntity<List<UsuarioBadgeResponse>> obtenerBadgesUsuario(
            @RequestHeader("Authorization") String authHeader) {
        Perfil perfil = obtenerPerfilAutenticado(authHeader);
        List<UsuarioBadgeResponse> badges = badgeService.obtenerBadgesUsuario(perfil);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/todos")
    public ResponseEntity<List<BadgeResponse>> obtenerTodosBadges(
            @RequestHeader("Authorization") String authHeader) {
        obtenerUsuarioAutenticado(authHeader); // Verificar autenticación
        List<BadgeResponse> badges = badgeService.obtenerTodosBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/progreso")
    public ResponseEntity<List<ProgresoResponse>> obtenerProgresoBadges(
            @RequestHeader("Authorization") String authHeader) {
        Perfil perfil = obtenerPerfilAutenticado(authHeader);
        List<ProgresoResponse> progreso = badgeService.obtenerProgresoBadges(perfil);
        return ResponseEntity.ok(progreso);
    }

    @PostMapping("/verificar")
    public ResponseEntity<BadgeObtenidoResponse> verificarNuevosBadges(
            @RequestHeader("Authorization") String authHeader) {
        Perfil perfil = obtenerPerfilAutenticado(authHeader);
        BadgeObtenidoResponse response = badgeService.verificarYOtorgarBadges(perfil);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticaUsuarioResponse> obtenerEstadisticasUsuario(
            @RequestHeader("Authorization") String authHeader) {
        Perfil perfil = obtenerPerfilAutenticado(authHeader);
        EstadisticaUsuarioResponse estadisticas = estadisticaService.obtenerEstadisticasUsuario(perfil);
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/perfil-completo")
    public ResponseEntity<PerfilConEstadisticasResponse> obtenerPerfilConEstadisticas(
            @RequestHeader("Authorization") String authHeader) {
        Perfil perfil = obtenerPerfilAutenticado(authHeader);
        
        PerfilConEstadisticasResponse response = new PerfilConEstadisticasResponse();
        
        // Información básica del perfil
        response.setPerfilId(perfil.getPerfilId());
        response.setNombreCompleto(perfil.getUsuario().getNombre() + " " + perfil.getUsuario().getApellido());
        response.setEmail(perfil.getUsuario().getEmail());
        response.setTipoPerfil(perfil.getTipoPerfil());
        response.setDescripcion(perfil.getDescripcion());
        response.setUbicacion(perfil.getUbicacion());
        response.setTelefono(perfil.getTelefono());
        response.setSitioWeb(perfil.getSitioWeb());
        response.setExperiencia(perfil.getExperiencia());
        response.setHabilidades(perfil.getHabilidades());
        response.setEducacion(perfil.getEducacion());
        response.setCertificaciones(perfil.getCertificaciones());
        response.setIntereses(perfil.getIntereses());
        
        // Estadísticas
        response.setEstadisticas(estadisticaService.obtenerEstadisticasUsuario(perfil));
        
        // Badges
        response.setBadges(badgeService.obtenerBadgesUsuario(perfil));
        response.setBadgesDisponibles(badgeService.obtenerTodosBadges());
        
        // Nivel y título
        Integer nivel = estadisticaService.calcularNivelUsuario(perfil);
        response.setNivelUsuario(nivel);
        response.setTitulo(estadisticaService.obtenerTituloUsuario(nivel));
        
        return ResponseEntity.ok(response);
    }

    private Usuario obtenerUsuarioAutenticado(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidacionException("Token de autorización requerido");
        }

        String token = authHeader.substring(7);
        if (token.trim().isEmpty()) {
            throw new ValidacionException("Token vacío");
        }

        try {
            String subject = tokenService.getSubject(token);
            if (subject == null || subject.trim().isEmpty()) {
                throw new ValidacionException("Token inválido");
            }

            Long numeroSesion = Long.parseLong(subject);
            Sesion sesion = sesionRepository.findById(numeroSesion)
                    .orElseThrow(() -> new ValidacionException("Sesión no encontrada"));

            if (!sesion.isActiva() || sesion.hasExpired()) {
                throw new ValidacionException("Sesión inactiva o expirada");
            }

            return sesion.getUsuario();
        } catch (NumberFormatException e) {
            throw new ValidacionException("Token de sesión inválido");
        } catch (Exception e) {
            throw new ValidacionException("Error al procesar el token: " + e.getMessage());
        }
    }

    private Perfil obtenerPerfilAutenticado(String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        return perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));
    }
}