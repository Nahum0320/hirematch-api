package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.CrearOfertaRequest;
import com.hirematch.hirematch_api.DTO.OfertaResponse;
import com.hirematch.hirematch_api.DTO.OfertaFeedResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;
    private final TokenService tokenService;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;

    public OfertaController(OfertaService ofertaService,
                            TokenService tokenService,
                            SesionRepository sesionRepository,
                            UsuarioRepository usuarioRepository,
                            PerfilRepository perfilRepository) {
        this.ofertaService = ofertaService;
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
    }

    @PostMapping
    public ResponseEntity<OfertaResponse> crearOferta(@Valid @RequestBody CrearOfertaRequest request,
                                                      @RequestHeader("Authorization") String authHeader) {

        // Obtener usuario autenticado y verificar que sea empresa
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        verificarTipoPerfil(usuario, "EMPRESA");

        OfertaResponse response = ofertaService.crearOferta(request, usuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<OfertaFeedResponse>> obtenerFeed(Pageable pageable,
                                                                @RequestHeader("Authorization") String authHeader) {

        // Obtener usuario autenticado y verificar que sea postulante
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        verificarTipoPerfil(usuario, "POSTULANTE");

        Page<OfertaFeedResponse> response = ofertaService.obtenerFeedParaUsuario(pageable, usuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfertaResponse> obtenerOferta(@PathVariable Long id,
                                                        @RequestHeader("Authorization") String authHeader) {

        // Obtener usuario autenticado (puede ser cualquier tipo)
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);

        OfertaResponse response = ofertaService.obtenerOfertaPorId(id);
        return ResponseEntity.ok(response);
    }


    /**
     * Endpoint público para obtener ofertas sin autenticación (para SEO, etc.)
     */
    @GetMapping("/publico")
    public ResponseEntity<Page<OfertaFeedResponse>> obtenerFeedPublico(Pageable pageable) {
        Page<OfertaFeedResponse> response = ofertaService.obtenerFeed(pageable);
        return ResponseEntity.ok(response);
    }



    /**
     * Extrae el usuario autenticado desde el JWT
     */
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
