package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.entity.*;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.repository.MatchRepository;
import com.hirematch.hirematch_api.repository.LikeRepository;
import com.hirematch.hirematch_api.repository.OfertaLaboralRepository;
import com.hirematch.hirematch_api.repository.PerfilRepository;
import com.hirematch.hirematch_api.repository.PostulantePorOfertaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final LikeRepository likeRepository;
    private final OfertaLaboralRepository ofertaRepository;
    private final PerfilRepository perfilRepository;
    private final PostulantePorOfertaRepository postulacionRepository;

    public MatchService(MatchRepository matchRepository, LikeRepository likeRepository,
                        OfertaLaboralRepository ofertaRepository, PerfilRepository perfilRepository,
                        PostulantePorOfertaRepository postulacionRepository) {
        this.matchRepository = matchRepository;
        this.likeRepository = likeRepository;
        this.ofertaRepository = ofertaRepository;
        this.perfilRepository = perfilRepository;
        this.postulacionRepository = postulacionRepository;
    }

    public List<Match> getMatchesByOfertaId(Long ofertaId) {
        return matchRepository.findByLike_Oferta_Id(ofertaId);
    }

    public Match save(Match match) {
        return matchRepository.save(match);
    }

    public void hacerMatch(Usuario usuario, Long likeId) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> {
                    System.out.println("Perfil no encontrado para usuario: ");
                    return new ValidacionException("Perfil no encontrado");
                });
        if (!"EMPRESA".equalsIgnoreCase(perfil.getTipoPerfil())) {
            System.out.println("Usuario no es EMPRESA: ");
            throw new ValidacionException("Solo empresas pueden hacer matches");
        }
        Like like = likeRepository.findById(likeId)
                .orElseThrow(() -> {
                    System.out.println("Like no encontrado: " + likeId);
                    return new ValidacionException("Like no encontrado");
                });
        if (matchRepository.findByLikeId(like.getId()).isPresent()) {
            System.out.println("Ya has hecho match con este postulante");
            throw new ValidacionException("Ya has hecho match con este postulante");
        }
        Optional<PostulantePorOferta> optPostulacion = postulacionRepository.findByPostulanteAndOferta(like.getPerfil(), like.getOferta());

        if (optPostulacion.isPresent()) {
            if (optPostulacion.get().getEstado() == EstadoPostulacion.PENDING) {
                Match match = new Match();
                match.setLike(like);
                match.setEmpresa(like.getOferta().getEmpresa());
                match.setFechaMatch(LocalDateTime.now());
                matchRepository.save(match);
                PostulantePorOferta postulacion = optPostulacion.get();
                postulacion.promoverToMatch();
                postulacionRepository.save(postulacion);
            }
            else {
                throw new ValidacionException("La postulación no está en estado Pendiente");
            }
        }
    }

    public List<PostulantePorOferta> getPostulacionesByUsuario(Long usuarioId, EstadoPostulacion estado) {
        if (estado == null) {
            return postulacionRepository.findByPostulanteUsuarioUsuarioIdOrderByFechaPostulacionDesc(usuarioId);
        } else {
            return postulacionRepository.findByUsuarioIdAndEstado(usuarioId, estado);
        }
    }
}