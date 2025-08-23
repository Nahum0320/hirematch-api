package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.PerfilUpdateRequest;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/profile")
public class PerfilController {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilController(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PerfilUpdateRequest request) {

        // Validación de usuario autenticado
        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        // Buscar perfil asociado al usuario
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("No existe perfil asociado a este usuario"));

        // Validaciones específicas de negocio
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

        // Actualizar campos solo si no son nulos
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
        // Validación básica para teléfono (solo números, espacios, guiones, paréntesis y +)
        return phone.matches("^[+\\d\\s\\-()]+$") && phone.replaceAll("[^\\d]", "").length() >= 7;
    }

    private boolean isValidUrl(String url) {
        // Validación básica para URL
        return url.matches("^(https?://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/.*)?$");
    }
}