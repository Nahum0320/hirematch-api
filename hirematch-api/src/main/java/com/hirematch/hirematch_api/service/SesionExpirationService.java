package com.hirematch.hirematch_api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.repository.SesionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SesionExpirationService {

    @Autowired
    private SesionRepository sesionRepository;

    // Ejecutar cada 5 minutos
    @Scheduled(fixedRate = 300000) 
    @Transactional
    public void expireOldSessions() {
        log.info("Iniciando proceso de expiración de sesiones...");
        
        // Buscar sesiones activas que han expirado
        List<Sesion> sesionesExpiradas = sesionRepository
            .findBySesionesActivasExpiradas(LocalDateTime.now());
        
        int count = 0;
        for (Sesion sesion : sesionesExpiradas) {
            sesion.expirar();
            sesionRepository.save(sesion);
            count++;
            log.debug("Sesión {} expirada para usuario {}", 
                     sesion.getNumeroSesion(), 
                     sesion.getUsuario().getLlaveUnica());
        }
        
        if (count > 0) {
            log.info("Se expiraron {} sesiones", count);
        }
    }

    // Método para expirar manualmente una sesión específica
    @Transactional
    public boolean expirarSesion(Long sesionId) {
        return sesionRepository.findById(sesionId)
            .map(sesion -> {
                if (sesion.isActiva()) {
                    sesion.expirar();
                    sesionRepository.save(sesion);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}