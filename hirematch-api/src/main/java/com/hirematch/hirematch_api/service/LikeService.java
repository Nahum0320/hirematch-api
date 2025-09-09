package com.hirematch.hirematch_api.service;
import com.hirematch.hirematch_api.DTO.LikeResponse;
import com.hirematch.hirematch_api.DTO.ProfileResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Empresa;
import com.hirematch.hirematch_api.entity.Like;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.EmpresaRepository;
import com.hirematch.hirematch_api.repository.LikeRepository;
import com.hirematch.hirematch_api.repository.OfertaLaboralRepository;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final PerfilRepository perfilRepository;
    private final OfertaLaboralRepository ofertaRepository;
    private final EmpresaRepository empresaRepository;
    public LikeService(LikeRepository likeRepository, PerfilRepository perfilRepository,
                       OfertaLaboralRepository ofertaRepository, EmpresaRepository empresaRepository) {
        this.likeRepository = likeRepository;
        this.perfilRepository = perfilRepository;
        this.ofertaRepository = ofertaRepository;
        this.empresaRepository = empresaRepository;
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
        Like like = new Like();
        like.setPerfil(perfil);
        like.setOferta(oferta);
        like.setFechaLike(LocalDateTime.now());
        likeRepository.save(like);
// El match se detecta automáticamente al existir el like; no se necesita acción adicional
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
        List<Like> likes = likeRepository.findByOferta(oferta);
        return likes.stream()
                .map(this::mapToLikeResponse)
                .collect(Collectors.toList());
    }

    private LikeResponse mapToLikeResponse(Like like) {
        LikeResponse response = new LikeResponse();
        response.setId(like.getId());
        response.setPerfilId(like.getPerfil().getPerfilId());
        response.setOfertaId(like.getOferta().getId());
        response.setFechaLike(like.getFechaLike());
        return response;
    }

    public Optional<Like> findById(Long likeId) {
        return likeRepository.findById(likeId);
    }   
}