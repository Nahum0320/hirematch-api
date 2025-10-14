package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.entity.UserStats;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.repository.UserStatsRepository;
import com.hirematch.hirematch_api.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserStatsController {

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SesionRepository sesionRepository;

    /**
     * Obtener las estadísticas del usuario autenticado
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(@RequestHeader("Authorization") String authHeader) {
        try {
            Usuario usuario = obtenerUsuarioAutenticado(authHeader);
            
            UserStats stats = userStatsRepository.findByUsuarioUsuarioId(usuario.getUsuarioId())
                .orElseGet(() -> {
                    // Si no existe, crear stats por defecto
                    UserStats newStats = new UserStats();
                    newStats.setUsuario(usuario);
                    newStats.setLikesRemaining(5); // Likes iniciales gratuitos
                    newStats.setSuperlikesRemaining(1); // 1 superlike gratis
                    newStats.setHasSubscription(false);
                    return userStatsRepository.save(newStats);
                });

            Map<String, Object> response = new HashMap<>();
            response.put("likesDisponibles", stats.getLikesRemaining());
            response.put("superlikesDisponibles", stats.getSuperlikesRemaining());
            response.put("suscripcionActiva", stats.getHasSubscription());
            response.put("nombreSuscripcion", stats.getSubscriptionType());
            response.put("fechaExpiracionSuscripcion", stats.getSubscriptionEndDate());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private Usuario obtenerUsuarioAutenticado(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidacionException("Token requerido");
        }

        String token = authHeader.substring(7);
        String subject = tokenService.getSubject(token);
        Long numeroSesion = Long.parseLong(subject);
        
        Sesion sesion = sesionRepository.findById(numeroSesion)
                .orElseThrow(() -> new ValidacionException("Sesión no encontrada"));

        if (!sesion.isActiva() || sesion.hasExpired()) {
            throw new ValidacionException("Sesión expirada");
        }

        return sesion.getUsuario();
    }
}