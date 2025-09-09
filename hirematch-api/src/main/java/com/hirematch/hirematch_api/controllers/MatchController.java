package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Match;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.LikeService;
import com.hirematch.hirematch_api.service.MatchService;
import com.hirematch.hirematch_api.service.OfertaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final LikeService likeService;

    private final OfertaService ofertaService;

    private final MatchService matchService;
    private final TokenService tokenService;
    private final SesionRepository sesionRepository;
    private final PerfilRepository perfilRepository;

    public MatchController(MatchService matchService, TokenService tokenService,
                           SesionRepository sesionRepository, PerfilRepository perfilRepository, OfertaService ofertaService, LikeService likeService) {
        this.matchService = matchService;
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
        this.perfilRepository = perfilRepository;
        this.ofertaService = ofertaService;
        this.likeService = likeService;
    }

    // Obtener matches por oferta
    @GetMapping("/oferta/{ofertaId}")
    public List<Match> getMatchesByOferta(@PathVariable Long ofertaId) {
        return matchService.getMatchesByOfertaId(ofertaId);
    }

    // Crear un nuevo match (ejemplo)
    @PostMapping("/like/{likeId}")
    public ResponseEntity<String> createMatch(@PathVariable Long likeId,
                              @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        verificarTipoPerfil(usuario, "EMPRESA");
        //verificar que la oferta a la que pertenece el like es de la empresa
        if (!ofertaService.perteneceAUsuario(likeService
        .findById(likeId)
        .get().
        getOferta()
        .getId(), usuario)) {
            throw new ValidacionException("El like no pertenece a la empresa");
        }
        matchService.hacerMatch(usuario, likeId);
        return ResponseEntity.ok("Match creado exitosamente");
    }
    private Usuario obtenerUsuarioAutenticado(String authHeader) {
        // Validar formato del header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidacionException("Token de autorización requerido");
        }

        // Extraer token
        String token = authHeader.substring(7);

        if (token.trim().isEmpty()) {
            throw new ValidacionException("Token vacío");
        }

        try {
            // Obtener subject del token (número de sesión)
            String subject = tokenService.getSubject(token);

            if (subject == null || subject.trim().isEmpty()) {
                throw new ValidacionException("Token inválido");
            }

            // Convertir a Long y obtener sesión
            Long numeroSesion = Long.parseLong(subject);

            Sesion sesion = sesionRepository.findById(numeroSesion)
                    .orElseThrow(() -> new ValidacionException("Sesión no encontrada"));

            // Verificar que la sesión esté activa
            if (!sesion.isActiva()) {
                throw new ValidacionException("Sesión inactiva o expirada");
            }

            // Verificar que la sesión no haya expirado por tiempo
            if (sesion.hasExpired()) {
                throw new ValidacionException("Sesión expirada");
            }

            return sesion.getUsuario();

        } catch (NumberFormatException e) {
            throw new ValidacionException("Token de sesión inválido");
        } catch (Exception e) {
            throw new ValidacionException("Error al procesar el token: " + e.getMessage());
        }
    }

    /**
     * Verifica que el usuario tenga el tipo de perfil requerido
     */
    private void verificarTipoPerfil(Usuario usuario, String tipoRequerido) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("El usuario no tiene un perfil configurado"));

        if (!tipoRequerido.equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado. Se requiere perfil de tipo " + tipoRequerido);
        }
    }
}