package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.PerfilUpdateRequest;
import com.hirematch.hirematch_api.DTO.ProfileRequest;
import com.hirematch.hirematch_api.DTO.ProfileResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.FotoPerfil;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.FotoPerfilRepository;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/profile")
public class PerfilController {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final FotoPerfilRepository fotoPerfilRepository;

    public PerfilController(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository, FotoPerfilRepository fotoPerfilRepository) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.fotoPerfilRepository = fotoPerfilRepository;
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
    public ResponseEntity<ProfileResponse> createProfile(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody ProfileRequest request) {

        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        if (perfilRepository.findByUsuario(usuario).isPresent()) {
            throw new ValidacionException("El usuario ya tiene un perfil creado");
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

        ProfileResponse response = new ProfileResponse();
        response.setPerfilId(savedPerfil.getPerfilId());
        response.setTipoPerfil(savedPerfil.getTipoPerfil());
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

        // Validate file type (JPEG or PNG)
        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new ValidacionException("Solo se permiten imágenes JPEG o PNG");
        }

        // Validate file size (e.g., max 5MB)
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
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PerfilUpdateRequest request) {

        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("No existe perfil asociado a este usuario"));

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

        perfilRepository.save(perfil);

        return ResponseEntity.ok(perfil);
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[+\\d\\s\\-()]+$") && phone.replaceAll("[^\\d]", "").length() >= 7;
    }

    private boolean isValidUrl(String url) {
        return url.matches("^(https?://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/.*)?$");
    }
}