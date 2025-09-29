package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.EstadisticaUsuarioResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.EstadisticaUsuario;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.EstadisticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final EstadisticaService estadisticaService;
    private final TokenService tokenService;
    private final SesionRepository sesionRepository;
    private final PerfilRepository perfilRepository;

    public UsuarioController(UsuarioRepository usuarioRepository, EstadisticaService estadisticaService,
                             TokenService tokenService, SesionRepository sesionRepository,
                             PerfilRepository perfilRepository) {
        this.usuarioRepository = usuarioRepository;
        this.estadisticaService = estadisticaService;
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
        this.perfilRepository = perfilRepository;
    }

    @PatchMapping("/activo")
    public ResponseEntity<String> updateUserActivo(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Usuario activado correctamente");
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticaUsuarioResponse> obtenerEstadisticas(
            @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));

        EstadisticaUsuarioResponse response = estadisticaService.obtenerEstadisticasUsuario(perfil);

        // Calcular métricas adicionales
        response.setTasaRespuesta(calcularTasaRespuesta(response));
        response.setRendimientoVsPromedio(calcularRendimientoVsPromedio(response));
        response.setPosicionRanking(calcularPosicionRanking(perfil));
        response.setFechaActualizacion(LocalDateTime.now());

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

    private Double calcularTasaRespuesta(EstadisticaUsuarioResponse response) {
        int totalLikesDados = response.getTotalLikesDados() + response.getTotalSuperlikesDados();
        int totalRespuestas = response.getTotalLikesRecibidos() + response.getTotalSuperlikesRecibidos() + response.getTotalMatches();
        return totalLikesDados > 0 ? (double) totalRespuestas / totalLikesDados : 0.0;
    }

    private String calcularRendimientoVsPromedio(EstadisticaUsuarioResponse response) {
        Double promedioMatches = estadisticaService.getEstadisticaUsuarioRepository().getPromedioMatches();
        Double promedioLikesDados = estadisticaService.getEstadisticaUsuarioRepository().getPromedioLikesDados();

        double tasaExitoUsuario = response.getTasaExito();
        double tasaExitoPromedio = promedioMatches != null && promedioLikesDados != null && promedioLikesDados > 0
                ? promedioMatches / promedioLikesDados
                : 0.0;

        if (tasaExitoUsuario > tasaExitoPromedio * 1.2) {
            return "Por encima del promedio";
        } else if (tasaExitoUsuario < tasaExitoPromedio * 0.8) {
            return "Por debajo del promedio";
        } else {
            return "En el promedio";
        }
    }

    private Double calcularPosicionRanking(Perfil perfil) {
        List<EstadisticaUsuario> topMatches = estadisticaService.getEstadisticaUsuarioRepository().findTopByMatches();
        long totalUsuarios = estadisticaService.getEstadisticaUsuarioRepository().count();
        if (totalUsuarios == 0) return 0.0;

        EstadisticaUsuario estadistica = estadisticaService.getEstadisticaUsuarioRepository().findByPerfil(perfil)
                .orElse(new EstadisticaUsuario());
        int posicion = topMatches.indexOf(estadistica) + 1;
        return posicion > 0 ? (1.0 - (double) posicion / totalUsuarios) * 100 : 0.0; // Percentil
    }
}