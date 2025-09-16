package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.EstadoPostulacion;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.PostulantePorOferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostulantePorOfertaRepository extends JpaRepository<PostulantePorOferta, Long> {

    List<PostulantePorOferta> findByOfertaOrderBySuperLikeDescFechaPostulacionDesc(OfertaLaboral oferta);

    Optional<PostulantePorOferta> findByPostulanteAndOferta(Perfil postulante, OfertaLaboral oferta);

    List<PostulantePorOferta> findByPostulanteAndEstado(Perfil postulante, EstadoPostulacion estado);

    long countByPostulanteAndSuperLikeTrueAndFechaPostulacionBetween(Perfil postulante, LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM PostulantePorOferta p WHERE p.postulante.usuario.usuarioId = :usuarioId AND p.estado = :estado")
    List<PostulantePorOferta> findByUsuarioIdAndEstado(@Param("usuarioId") Long usuarioId, @Param("estado") EstadoPostulacion estado);

    List<PostulantePorOferta> findByPostulanteUsuarioUsuarioIdOrderByFechaPostulacionDesc(Long usuarioId);
    
}
