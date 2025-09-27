package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.LikeResponse;
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
public class LikeService {
    private final LikeRepository likeRepository;
    private final PerfilRepository perfilRepository;
    private final OfertaLaboralRepository ofertaRepository;
    private final EmpresaRepository empresaRepository;
    private final PostulantePorOfertaRepository postulacionRepository;
    private final PassRepository passRepository;
    private final EstadisticaService estadisticaService;

    public LikeService(LikeRepository likeRepository, PerfilRepository perfilRepository,
                       OfertaLaboralRepository ofertaRepository, EmpresaRepository empresaRepository,
                       PostulantePorOfertaRepository postulacionRepository, PassRepository passRepository,
                       EstadisticaService estadisticaService) {
        this.likeRepository = likeRepository;
        this.perfilRepository = perfilRepository;
        this.ofertaRepository = ofertaRepository;
        this.empresaRepository = empresaRepository;
        this.postulacionRepository = postulacionRepository;
        this.passRepository = passRepository;
        this.estadisticaService = estadisticaService;
    }

    public void darLike(Usuario usuario, Long ofertaId) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));
        if (!"POSTULANTE".equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("Solo postulantes pueden dar likes a ofertas");
        }
        OfertaLaboral oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));
        if (likeRepository.findByPerfilAndOferta(perfil, oferta).isPresent()) {
            throw new ValidacionException("Ya has dado like a esta oferta");
        }
        if (passRepository.findByPerfilAndOferta(perfil, oferta).isPresent()) {
            throw new ValidacionException("Ya has dado pass a esta oferta");
        }
        if (postulacionRepository.findByPostulanteAndOferta(perfil, oferta).isPresent()) {
            throw new ValidacionException("Ya has postulado a esta oferta");
        }
        Like like = new Like();
        like.setPerfil(perfil);
        like.setOferta(oferta);
        like.setFechaLike(LocalDateTime.now());
        likeRepository.save(like);

        // Crear PostulantePorOferta
        PostulantePorOferta postulacion = new PostulantePorOferta();
        postulacion.setPostulante(perfil);
        postulacion.setOferta(oferta);
        postulacion.setEstado(EstadoPostulacion.PENDING);
        postulacion.setSuperLike(false);
        postulacionRepository.save(postulacion);

        // Incrementar aplicaciones recibidas
        oferta.incrementarAplicaciones();
        ofertaRepository.save(oferta);

        // Actualizar estadísticas
        estadisticaService.actualizarEstadisticaLikeDado(perfil);
        estadisticaService.actualizarEstadisticaLikeRecibido(oferta.getEmpresa().getPerfil());
    }

    public void darSuperLike(Usuario usuario, Long ofertaId) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));
        if (!"POSTULANTE".equalsIgnoreCase(perfil.getTipoPerfil())) {
            throw new ValidacionException("Solo postulantes pueden dar superlikes a ofertas");
        }
        OfertaLaboral oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));

        // Verificar si ya postuló
        if (postulacionRepository.findByPostulanteAndOferta(perfil, oferta).isPresent()) {
            throw new ValidacionException("Ya has interactuado con esta oferta");
        }
        if (passRepository.findByPerfilAndOferta(perfil, oferta).isPresent()) {
            throw new ValidacionException("Ya has dado pass a esta oferta");
        }

        // Verificar límite diario (5 superlikes por día)
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long count = postulacionRepository.countByPostulanteAndSuperLikeTrueAndFechaPostulacionBetween(perfil, startOfDay, endOfDay);
        if (count >= 5) {
            throw new ValidacionException("Límite diario de superlikes alcanzado (5 por día)");
        }

        // Crear Like
        Like like = new Like();
        like.setPerfil(perfil);
        like.setOferta(oferta);
        like.setFechaLike(LocalDateTime.now());
        likeRepository.save(like);

        // Crear PostulantePorOferta con superLike
        PostulantePorOferta postulacion = new PostulantePorOferta();
        postulacion.setPostulante(perfil);
        postulacion.setOferta(oferta);
        postulacion.setSuperLike();
        postulacionRepository.save(postulacion);

        // Incrementar aplicaciones recibidas
        oferta.incrementarAplicaciones();
        ofertaRepository.save(oferta);

        // Actualizar estadísticas
        estadisticaService.actualizarEstadisticaSuperlikeDado(perfil);
        estadisticaService.actualizarEstadisticaSuperlikeRecibido(oferta.getEmpresa().getPerfil());
    }

    public Page<ProfileResponse> getMatchesForEmpresa(Usuario usuarioEmpresa, Pageable pageable) {
        Empresa empresa = empresaRepository.findByUsuario(usuarioEmpresa)
                .orElseThrow(() -> new ValidacionException("Empresa no encontrada para el usuario"));
        // Obtener todos los likes para las ofertas de esta empresa
        List<Like> likes = likeRepository.findByOferta_Empresa_EmpresaId(empresa.getEmpresaId());
        // Extraer perfiles únicos
        List<Perfil> perfiles = likes.stream()
                .map(Like::getPerfil)
                .distinct()
                .collect(Collectors.toList());
        // Convertir a ProfileResponse (asumiendo que tienes un mapper o usas el constructor)
        List<ProfileResponse> responses = perfiles.stream()
                .map(this::mapToProfileResponse) // Implementa este método según tu DTO
                .collect(Collectors.toList());
        // Paginación manual (ya que usamos distinct)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<ProfileResponse> pagedResponses = responses.subList(start, end);
        return new PageImpl<>(pagedResponses, pageable, responses.size());
    }

    // Método auxiliar para mapear Perfil a ProfileResponse
    private ProfileResponse mapToProfileResponse(Perfil perfil) {
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
        // Agrega fotoUrl si es necesario, similar a tu controlador
        return response;
    }

    public List<LikeResponse> getLikesByOferta(Long ofertaId) {
        OfertaLaboral oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));
        List<PostulantePorOferta> postulaciones = postulacionRepository.findByOfertaOrderBySuperLikeDescFechaPostulacionDesc(oferta);
        return postulaciones.stream()
                .map(this::mapToLikeResponse)
                .collect(Collectors.toList());
    }

    private LikeResponse mapToLikeResponse(PostulantePorOferta postulacion) {
    Like like = likeRepository.findByPerfilAndOferta(postulacion.getPostulante(), postulacion.getOferta())
            .orElseThrow(() -> new ValidacionException("No se encontró el like asociado a la postulación"));

    LikeResponse response = new LikeResponse();
    response.setLikeId(like.getId()); // Ahora usamos el ID del like
    response.setUsuarioEmail(postulacion.getPostulante().getUsuario().getEmail());
    response.setFechaLike(like.getFechaLike()); // Usamos la fecha del like
    response.setTipoLike(postulacion.isSuperLike() ? "SUPER_LIKE" : "LIKE");
    
    response.setPerfilId(postulacion.getPostulante().getPerfilId());
    response.setOfertaId(postulacion.getOferta().getId());
    response.setEstado(postulacion.getEstado().getDescripcion());
    response.setSuperLike(postulacion.isSuperLike());
    
    return response;
}

    public Optional<Like> findById(Long likeId) {
        return likeRepository.findById(likeId);
    }
}