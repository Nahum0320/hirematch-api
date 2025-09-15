package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.PassResponse;
import com.hirematch.hirematch_api.DTO.ProfileResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Pass;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.PassService;
import com.hirematch.hirematch_api.service.LikeService;
import com.hirematch.hirematch_api.service.OfertaService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hirematch.hirematch_api.entity.EstadoPostulacion;


@RestController
@RequestMapping("/api/passes")
public class PassController {

    private final PassService passService;
    private final TokenService tokenService;
    private final SesionRepository sesionRepository;
    private final PerfilRepository perfilRepository;
    private final LikeService likeService;
    private final OfertaService ofertaService;

    public PassController(PassService passService, TokenService tokenService,
                          SesionRepository sesionRepository, PerfilRepository perfilRepository,
                          LikeService likeService, OfertaService ofertaService) {
        this.passService = passService;
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
        this.perfilRepository = perfilRepository;
        this.likeService = likeService;
        this.ofertaService = ofertaService;
    }

    @PostMapping("/oferta/{ofertaId}")
    public ResponseEntity<String> darPass(@PathVariable Long ofertaId,
                                          @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        verificarTipoPerfil(usuario, "POSTULANTE");
        passService.darPass(usuario, ofertaId);
        return ResponseEntity.ok("Pass registrado correctamente");
    }

    @PostMapping("/like/{likeId}")
    public ResponseEntity<String> darLike(@PathVariable Long likeId,
                                          @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        verificarTipoPerfil(usuario, "Empresa");
        try {
            var likeOpt = likeService.findById(likeId);
            if (likeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("El like con ID " + likeId + " no existe");
            }

            var like = likeOpt.get();
            var ofertaId = like.getOferta().getId();

            if (!ofertaService.perteneceAUsuario(ofertaId, usuario)) {
                throw new ValidacionException("La oferta asociada al like no pertenece a la empresa actual");
            }
            passService.darPassEmpresa(like.getPerfil(), like.getOferta());
            return ResponseEntity.ok("Like rechazado exitosamente");
        } catch (ValidacionException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        
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

    private void verificarTipoPerfil(Usuario usuario, String tipoRequerido) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("El usuario no tiene un perfil configurado"));

        if (!tipoRequerido.equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("Acceso denegado. Se requiere perfil de tipo " + tipoRequerido);
        }
    }
}