package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.LikeResponse;
import com.hirematch.hirematch_api.DTO.ProfileResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Like;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.LikeService;
import com.hirematch.hirematch_api.service.OfertaService;
import com.hirematch.hirematch_api.service.UserStatsService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hirematch.hirematch_api.entity.EstadoPostulacion;


@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;
    private final TokenService tokenService;
    private final SesionRepository sesionRepository;
    private final PerfilRepository perfilRepository;
    private final OfertaService ofertaService;
    private UserStatsService userStatsService;

    public LikeController(LikeService likeService, TokenService tokenService,
                          SesionRepository sesionRepository, PerfilRepository perfilRepository,
                          OfertaService ofertaService) {
        this.likeService = likeService;
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
        this.perfilRepository = perfilRepository;
        this.ofertaService = ofertaService;
    }

    @PostMapping("/oferta/{ofertaId}")
    public ResponseEntity<String> darLike(@PathVariable Long ofertaId,
                                          @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        boolean canSendLike = userStatsService.useLike(usuario.getUsuarioId());
        
        if (!canSendLike) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes suficientes likes disponibles. Compra más likes o una suscripción.");
        }

        likeService.darLike(usuario, ofertaId);
        return ResponseEntity.ok("Like registrado correctamente");
    }

    @PostMapping("/swipe/superlike/{ofertaId}")
    public ResponseEntity<String> darSuperLike(@PathVariable Long ofertaId,
                                               @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);

        boolean canSendLike = userStatsService.useSuperlike(usuario.getUsuarioId());
        
        if (!canSendLike) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes suficientes superlikes disponibles. Compra más superlikes o una suscripción.");
        }

        likeService.darSuperLike(usuario, ofertaId);
        return ResponseEntity.ok("Superlike registrado correctamente");
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

    @GetMapping("/oferta/{ofertaId}")
    public ResponseEntity<List<LikeResponse>> getLikesByOferta(@PathVariable Long ofertaId,
                                                               @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        verificarTipoPerfil(usuario, "EMPRESA");
        //verificar que la oferta le pertenezca a la empresa
        if (!ofertaService.perteneceAUsuario(ofertaId, usuario)) {
            throw new ValidacionException("La oferta no pertenece a la empresa");
        }
        return ResponseEntity.ok(likeService.getLikesByOferta(ofertaId));
    }

    private void verificarTipoPerfil(Usuario usuario, String tipoRequerido) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("El usuario no tiene un perfil configurado"));

        if (!tipoRequerido.equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado. Se requiere perfil de tipo " + tipoRequerido);
        }
    }
}