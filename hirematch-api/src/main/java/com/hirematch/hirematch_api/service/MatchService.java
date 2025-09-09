package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.entity.Match;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Empresa;
import com.hirematch.hirematch_api.entity.Like;
import com.hirematch.hirematch_api.repository.MatchRepository;
import org.springframework.stereotype.Service;
import com.hirematch.hirematch_api.repository.LikeRepository;
import com.hirematch.hirematch_api.repository.OfertaLaboralRepository;
import com.hirematch.hirematch_api.repository.PerfilRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final LikeRepository likeRepository;
    private final OfertaLaboralRepository ofertaRepository;
    private final PerfilRepository perfilRepository;    

    public MatchService(MatchRepository matchRepository, LikeRepository likeRepository,
                        OfertaLaboralRepository ofertaRepository, PerfilRepository perfilRepository) {
        this.matchRepository = matchRepository;
        this.likeRepository = likeRepository;
        this.ofertaRepository = ofertaRepository;
        this.perfilRepository = perfilRepository;
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
        if (likeRepository.findByPerfilAndOferta(perfil, like.getOferta()).isPresent()) {
            System.out.println("Ya has hecho match con este postulante");
            throw new ValidacionException("Ya has hecho match con este postulante");
        }
        Match match = new Match();
        match.setLike(like);
        match.setEmpresa(like.getOferta().getEmpresa());
        match.setFechaMatch(LocalDateTime.now());
        matchRepository.save(match);
    }
}