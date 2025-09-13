package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.DTO.PerfilPublicoResponse;
import com.hirematch.hirematch_api.DTO.PerfilUpdateRequest;
import com.hirematch.hirematch_api.entity.FotoPerfil;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.FotoPerfilRepository;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Base64;
import java.util.Optional;

@Service
public class PerfilService {

    @Autowired
    private PerfilRepository perfilRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private FotoPerfilRepository fotoPerfilRepository;

    public PerfilService(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository, FotoPerfilRepository fotoPerfilRepository) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.fotoPerfilRepository = fotoPerfilRepository;
    }

    @Transactional
    public Perfil actualizarPerfil(Usuario usuario, PerfilUpdateRequest request) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado para el usuario"));

        if (request.getDescripcion() != null) perfil.setDescripcion(request.getDescripcion());
        if (request.getUbicacion() != null) perfil.setUbicacion(request.getUbicacion());
        if (request.getHabilidades() != null) perfil.setHabilidades(request.getHabilidades());
        if (request.getTelefono() != null) perfil.setTelefono(request.getTelefono());
        if (request.getSitioWeb() != null) perfil.setSitioWeb(request.getSitioWeb());
        if (request.getExperiencia() != null) perfil.setExperiencia(request.getExperiencia());
        if (request.getEducacion() != null) perfil.setEducacion(request.getEducacion());
        if (request.getCertificaciones() != null) perfil.setCertificaciones(request.getCertificaciones());
        if (request.getIntereses() != null) perfil.setIntereses(request.getIntereses());
        //if (request.getFotoUrl() != null) perfil.setFotoUrl(request.getFotoUrl());

        return perfilRepository.save(perfil);
    }

    public PerfilPublicoResponse getPerfilPublicoPorEmail(String email) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ValidacionException("Usuario no encontrado"));
        
        // Obtener el perfil del usuario
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado para el usuario"));
        
        // Verificar que es un perfil de candidato (no empresa)
        if ("EMPRESA".equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("No se puede acceder al perfil de una empresa");
        }
        
        // Crear la respuesta
        PerfilPublicoResponse response = new PerfilPublicoResponse();
        response.setPerfilId(perfil.getPerfilId());
        response.setNombreCompleto(usuario.getNombre() + " " + usuario.getApellido());
        response.setEmail(usuario.getEmail());
        response.setTipoPerfil(perfil.getTipoPerfil());
        response.setDescripcion(perfil.getDescripcion());
        response.setUbicacion(perfil.getUbicacion());
        response.setHabilidades(perfil.getHabilidades());
        response.setExperiencia(perfil.getExperiencia());
        response.setEducacion(perfil.getEducacion());
        response.setCertificaciones(perfil.getCertificaciones());
        response.setIntereses(perfil.getIntereses());
        
        // Agregar foto si existe
        Optional<FotoPerfil> foto = fotoPerfilRepository.findByPerfil(perfil);
        if (foto.isPresent()) {
            String base64Image = Base64.getEncoder().encodeToString(foto.get().getFoto());
            response.setFotoUrl("data:image/jpeg;base64," + base64Image);
        }
        
        return response;
    }
}

