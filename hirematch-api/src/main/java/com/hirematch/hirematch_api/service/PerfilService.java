package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.PerfilUpdateRequest;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;

    public PerfilService(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
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
}

