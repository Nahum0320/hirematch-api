package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.EstadisticasEmpresaResponse;
import com.hirematch.hirematch_api.DTO.PerfilPublicoResponse;
import com.hirematch.hirematch_api.DTO.PerfilUpdateRequest;
import com.hirematch.hirematch_api.DTO.ProfileRequest;
import com.hirematch.hirematch_api.DTO.ProfileResponse;
import com.hirematch.hirematch_api.entity.Empresa;
import com.hirematch.hirematch_api.entity.FotoPerfil;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.repository.EmpresaRepository;
import com.hirematch.hirematch_api.repository.FotoPerfilRepository;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;


@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FotoPerfilRepository fotoPerfilRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private com.hirematch.hirematch_api.service.PerfilService perfilService;

    @Autowired
    private OfertaService ofertaService;

    private final TokenService tokenService;
    private final SesionRepository sesionRepository;

    public PerfilController(TokenService tokenService, SesionRepository sesionRepository) {
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[+\\d\\s\\-()]+$") && phone.replaceAll("[^\\d]", "").length() >= 7;
    }

    private boolean isValidUrl(String url) {
        return url != null && url.matches("^(https?://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/.*)?$");
    }

    @GetMapping("/me")
public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal Usuario usuario) {
    if (usuario == null) {
        throw new ValidacionException("Usuario no autenticado");
    }

    Perfil perfil = perfilRepository.findByUsuario(usuario)
            .orElseThrow(() -> new ValidacionException("No existe perfil asociado a este usuario"));

    ProfileResponse response = new ProfileResponse();
    response.setPerfilId(perfil.getPerfilId());
    response.setTipoPerfil(perfil.getTipoPerfil());
    response.setDescripcion(perfil.getDescripcion());
    response.setUbicacion(perfil.getUbicacion());
    response.setHabilidades(perfil.getHabilidades());
    response.setTelefono(perfil.getTelefono());
    response.setSitioWeb(perfil.getSitioWeb());
    response.setExperiencia(perfil.getExperiencia());
    response.setEducacion(perfil.getEducacion());
    response.setCertificaciones(perfil.getCertificaciones());
    response.setIntereses(perfil.getIntereses());

    // Include nombreEmpresa and empresaId if empresa profile
    if ("empresa".equalsIgnoreCase(perfil.getTipoPerfil())) {
        empresaRepository.findByUsuarioUsuarioId(usuario.getUsuarioId())
                .stream().findFirst()
                .ifPresent(empresa -> {
                    response.setNombreEmpresa(empresa.getNombreEmpresa());
                    response.setEmpresaId(empresa.getEmpresaId()); // Add empresaId
                });
    }

    // Include photo as base64 if available
    fotoPerfilRepository.findByPerfil(perfil).ifPresent(foto -> {
        String base64Image = Base64.getEncoder().encodeToString(foto.getFoto());
        response.setFotoUrl("data:image/jpeg;base64," + base64Image);
    });

    response.setMensaje("Perfil recuperado correctamente");
    return ResponseEntity.ok(response);
}

    @GetMapping("/{perfilId}/foto")
    public ResponseEntity<byte[]> getProfilePhoto(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long perfilId) {
        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));

        if (!perfil.getUsuario().getUsuarioId().equals(usuario.getUsuarioId())) {
            throw new ValidacionException("No autorizado para ver esta foto");
        }

        FotoPerfil foto = fotoPerfilRepository.findByPerfil(perfil)
                .orElseThrow(() -> new ValidacionException("Foto no encontrada"));

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(foto.getFoto());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProfileResponse> createProfile(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody ProfileRequest request) {

        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        if (perfilRepository.findByUsuario(usuario).isPresent()) {
            throw new ValidacionException("El usuario ya tiene un perfil creado");
        }

        if ("empresa".equalsIgnoreCase(request.getTipoPerfil())) {
            if (request.getNombreEmpresa() == null || request.getNombreEmpresa().trim().isEmpty()) {
                throw new ValidacionException("El nombre de la empresa es obligatorio para perfiles de tipo empresa");
            }
            if (empresaRepository.existsByNombreEmpresa(request.getNombreEmpresa().trim())) {
                throw new ValidacionException("El nombre de la empresa ya está registrado");
            }
        }

        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            if (!isValidPhone(request.getTelefono())) {
                throw new ValidacionException("El formato del teléfono no es válido");
            }
        }

        if (request.getSitioWeb() != null && !request.getSitioWeb().trim().isEmpty()) {
            if (!isValidUrl(request.getSitioWeb())) {
                throw new ValidacionException("El formato de la URL del sitio web no es válido");
            }
        }

        Perfil perfil = new Perfil();
        perfil.setUsuario(usuario);
        perfil.setTipoPerfil(request.getTipoPerfil());
        perfil.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        perfil.setUbicacion(request.getUbicacion() != null ? request.getUbicacion().trim() : null);
        perfil.setHabilidades(request.getHabilidades() != null ? request.getHabilidades().trim() : null);
        perfil.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        perfil.setSitioWeb(request.getSitioWeb() != null ? request.getSitioWeb().trim() : null);
        perfil.setExperiencia(request.getExperiencia() != null ? request.getExperiencia().trim() : null);
        perfil.setEducacion(request.getEducacion() != null ? request.getEducacion().trim() : null);
        perfil.setCertificaciones(request.getCertificaciones() != null ? request.getCertificaciones().trim() : null);
        perfil.setIntereses(request.getIntereses() != null ? request.getIntereses().trim() : null);

        Perfil savedPerfil = perfilRepository.save(perfil);

        if ("empresa".equalsIgnoreCase(savedPerfil.getTipoPerfil())) {
            Empresa empresa = new Empresa();
            empresa.setUsuario(usuario);
            empresa.setNombreEmpresa(request.getNombreEmpresa().trim());
            empresa.setDescripcion(savedPerfil.getDescripcion());
            empresa.setSitioWeb(savedPerfil.getSitioWeb());
            empresa.setPerfil(savedPerfil);
            empresaRepository.save(empresa);
        }

        ProfileResponse response = new ProfileResponse();
        response.setPerfilId(savedPerfil.getPerfilId());
        response.setTipoPerfil(savedPerfil.getTipoPerfil());
        response.setNombreEmpresa(request.getNombreEmpresa());
        response.setDescripcion(savedPerfil.getDescripcion());
        response.setUbicacion(savedPerfil.getUbicacion());
        response.setHabilidades(savedPerfil.getHabilidades());
        response.setTelefono(savedPerfil.getTelefono());
        response.setSitioWeb(savedPerfil.getSitioWeb());
        response.setExperiencia(savedPerfil.getExperiencia());
        response.setEducacion(savedPerfil.getEducacion());
        response.setCertificaciones(savedPerfil.getCertificaciones());
        response.setIntereses(savedPerfil.getIntereses());
        response.setMensaje("Perfil creado correctamente");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{perfilId}/foto")
    public ResponseEntity<String> uploadProfilePhoto(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long perfilId,
            @RequestParam("foto") MultipartFile file) throws IOException {

        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));

        if (!perfil.getUsuario().getUsuarioId().equals(usuario.getUsuarioId())) {
            throw new ValidacionException("No autorizado para actualizar este perfil");
        }

        if (file.isEmpty()) {
            throw new ValidacionException("El archivo de foto es requerido");
        }

        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new ValidacionException("Solo se permiten imágenes JPEG o PNG");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ValidacionException("La imagen no puede exceder los 5MB");
        }

        FotoPerfil fotoPerfil = new FotoPerfil();
        fotoPerfil.setPerfil(perfil);
        fotoPerfil.setFoto(file.getBytes());

        fotoPerfilRepository.save(fotoPerfil);

        return ResponseEntity.ok("Foto de perfil subida correctamente");
    }

    @PutMapping("/me")
    @Transactional
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PerfilUpdateRequest request) {

        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("No existe perfil asociado a este usuario"));

        if ("empresa".equalsIgnoreCase(perfil.getTipoPerfil())) {
            if (request.getNombreEmpresa() != null && !request.getNombreEmpresa().trim().isEmpty()) {
                Empresa existingEmpresa = empresaRepository.findByUsuarioUsuarioId(usuario.getUsuarioId())
                        .stream().findFirst().orElse(null);
                if (existingEmpresa != null && !existingEmpresa.getNombreEmpresa().equals(request.getNombreEmpresa().trim())) {
                    if (empresaRepository.existsByNombreEmpresa(request.getNombreEmpresa().trim())) {
                        throw new ValidacionException("El nombre de la empresa ya está registrado");
                    }
                }
            } else if (request.getNombreEmpresa() != null && request.getNombreEmpresa().trim().isEmpty()) {
                throw new ValidacionException("El nombre de la empresa no puede estar vacío para perfiles de tipo empresa");
            }
        }

        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            if (!isValidPhone(request.getTelefono())) {
                throw new ValidacionException("El formato del teléfono no es válido");
            }
        }

        if (request.getSitioWeb() != null && !request.getSitioWeb().trim().isEmpty()) {
            if (!isValidUrl(request.getSitioWeb())) {
                throw new ValidacionException("El formato de la URL del sitio web no es válido");
            }
        }

        if (request.getDescripcion() != null && request.getDescripcion().length() > 1000) {
            throw new ValidacionException("La descripción no puede exceder los 1000 caracteres");
        }

        if (request.getUbicacion() != null && request.getUbicacion().length() > 255) {
            throw new ValidacionException("La ubicación no puede exceder los 255 caracteres");
        }

        if (request.getDescripcion() != null) perfil.setDescripcion(request.getDescripcion().trim());
        if (request.getUbicacion() != null) perfil.setUbicacion(request.getUbicacion().trim());
        if (request.getHabilidades() != null) perfil.setHabilidades(request.getHabilidades().trim());
        if (request.getTelefono() != null) perfil.setTelefono(request.getTelefono().trim());
        if (request.getSitioWeb() != null) perfil.setSitioWeb(request.getSitioWeb().trim());
        if (request.getExperiencia() != null) perfil.setExperiencia(request.getExperiencia().trim());
        if (request.getEducacion() != null) perfil.setEducacion(request.getEducacion().trim());
        if (request.getCertificaciones() != null) perfil.setCertificaciones(request.getCertificaciones().trim());
        if (request.getIntereses() != null) perfil.setIntereses(request.getIntereses().trim());

        Perfil savedPerfil = perfilRepository.save(perfil);

        if ("empresa".equalsIgnoreCase(savedPerfil.getTipoPerfil())) {
            Empresa empresa = empresaRepository.findByUsuarioUsuarioId(usuario.getUsuarioId())
                    .stream().findFirst().orElse(new Empresa());
            empresa.setUsuario(usuario);
            if (request.getNombreEmpresa() != null) {
                empresa.setNombreEmpresa(request.getNombreEmpresa().trim());
            }
            empresa.setDescripcion(savedPerfil.getDescripcion());
            empresa.setSitioWeb(savedPerfil.getSitioWeb());
            empresaRepository.save(empresa);
        }

        ProfileResponse response = new ProfileResponse();
        response.setPerfilId(savedPerfil.getPerfilId());
        response.setTipoPerfil(savedPerfil.getTipoPerfil());
        response.setNombreEmpresa(request.getNombreEmpresa());
        response.setDescripcion(savedPerfil.getDescripcion());
        response.setUbicacion(savedPerfil.getUbicacion());
        response.setHabilidades(savedPerfil.getHabilidades());
        response.setTelefono(savedPerfil.getTelefono());
        response.setSitioWeb(savedPerfil.getSitioWeb());
        response.setExperiencia(savedPerfil.getExperiencia());
        response.setEducacion(savedPerfil.getEducacion());
        response.setCertificaciones(savedPerfil.getCertificaciones());
        response.setIntereses(savedPerfil.getIntereses());
        response.setMensaje("Perfil actualizado correctamente");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/empresa/id")
    public ResponseEntity<Long> getEmpresaId(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }
        Empresa empresa = empresaRepository.findByUsuarioUsuarioId(usuario.getUsuarioId())
                .stream().findFirst()
                .orElseThrow(() -> new ValidacionException("No existe empresa asociada a este usuario"));
        return ResponseEntity.ok(empresa.getEmpresaId());
    }

    @GetMapping("/empresa/estadisticas")
    public ResponseEntity<EstadisticasEmpresaResponse> obtenerEstadisticasEmpresa(
            @RequestHeader("Authorization") String authHeader) {
        
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        verificarTipoPerfil(usuario, "EMPRESA");
        
        EstadisticasEmpresaResponse response = ofertaService.obtenerEstadisticasEmpresa(usuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/publico")
public ResponseEntity<PerfilPublicoResponse> getPerfilPublicoPorEmail(
        @RequestParam String email,
        @RequestHeader("Authorization") String authHeader) {
    
    // Obtener usuario autenticado (reutiliza tu método existente)
    Usuario usuarioAutenticado = obtenerUsuarioAutenticado(authHeader);
    
    // Verificar que sea una empresa
    verificarTipoPerfil(usuarioAutenticado, "EMPRESA");
    
    PerfilPublicoResponse perfil = perfilService.getPerfilPublicoPorEmail(email);
    
    return ResponseEntity.ok(perfil);
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