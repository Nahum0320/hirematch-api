package com.hirematch.hirematch_api.service;
import org.springframework.stereotype.Service;

import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.SesionRepository;

import jakarta.transaction.Transactional;

@Service
public class SesionService {
    
    private final SesionRepository sesionRepository;
    public SesionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Transactional
    public Sesion crearSesion(Usuario usuario) {
            Sesion sesion = new Sesion();
            sesion.activar();
            sesion.setUsuario(usuario);
            sesionRepository.save(sesion);
            return sesion;
    }
    @Transactional public void cerrarSesion(Long id) {
        sesionRepository.findById(id).ifPresent(sesion -> {
            sesion.cancelar();
            sesionRepository.save(sesion);
        });
    }

    
    public Sesion obtenerSesion(Long id) {
        return sesionRepository.findById(id).orElse(null);
    }

}
