package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.PassResponse;
import com.hirematch.hirematch_api.DTO.ProfileResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.*;
import com.hirematch.hirematch_api.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PassService {
    private final PassRepository passRepository;
    private final PerfilRepository perfilRepository;
    private final OfertaLaboralRepository ofertaRepository;
    private final LikeRepository likeRepository;
    private final PostulantePorOfertaRepository postulacionRepository;
    private final EstadisticaService estadisticaService;

    public PassService(PassRepository passRepository, PerfilRepository perfilRepository,
                       OfertaLaboralRepository ofertaRepository, LikeRepository likeRepository,
                       PostulantePorOfertaRepository postulacionRepository,
                       EstadisticaService estadisticaService) {
        this.passRepository = passRepository;
        this.perfilRepository = perfilRepository;
        this.ofertaRepository = ofertaRepository;
        this.likeRepository = likeRepository;
        this.postulacionRepository = postulacionRepository;
        this.estadisticaService = estadisticaService;
    }

    public void darPass(Usuario usuario, Long ofertaId) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));
        if (!"POSTULANTE".equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("Solo postulantes pueden dar passes a ofertas");
        }
        OfertaLaboral oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));
        if (passRepository.findByPerfilAndOferta(perfil, oferta).isPresent()) {
            throw new ValidacionException("Ya has dado pass a esta oferta");
        }
        if (likeRepository.findByPerfilAndOferta(perfil, oferta).isPresent()) {
            throw new ValidacionException("Ya has dado like a esta oferta");
        }
        if (postulacionRepository.findByPostulanteAndOferta(perfil, oferta).isPresent()) {
            throw new ValidacionException("Ya has postulado a esta oferta");
        }
        Pass pass = new Pass();
        pass.setPerfil(perfil);
        pass.setOferta(oferta);
        pass.setFechaPass(LocalDateTime.now());
        passRepository.save(pass);

        // Actualizar estadísticas
        estadisticaService.actualizarEstadisticaRechazo(perfil, oferta.getEmpresa().getPerfil());
    }

    public Optional<Pass> findById(Long passId) {
        return passRepository.findById(passId);
    }

    public void darPassEmpresa(Perfil perfil, OfertaLaboral oferta) {
        PostulantePorOferta postulacion = postulacionRepository.findByPostulanteAndOferta(perfil, oferta)
                .orElseThrow(() -> new ValidacionException("Postulación no encontrada"));
        if(postulacion.getEstado() == EstadoPostulacion.REJECTED) {
            throw new ValidacionException("La postulación ya ha sido rechazada");
        }
        if(postulacion.getEstado() == EstadoPostulacion.ACCEPTED) {
            throw new ValidacionException("La postulación ya ha sido aceptada");
        }
        if(postulacion.getEstado() == EstadoPostulacion.MATCHED) {
            throw new ValidacionException("Ya has hecho match con este postulante");
        }
        if(postulacion.getEstado() == EstadoPostulacion.PENDING) {
            postulacion.setEstado(EstadoPostulacion.REJECTED);
            postulacionRepository.save(postulacion);
        }    
    }

}