package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.OfertaGuardada;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Perfil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfertaGuardadaRepository extends JpaRepository<OfertaGuardada, Long> {

    Optional<OfertaGuardada> findByPerfilAndOferta(Perfil perfil, OfertaLaboral oferta);

    Page<OfertaGuardada> findByPerfilOrderByFechaGuardadoDesc(Perfil perfil, Pageable pageable);

    boolean existsByPerfilAndOferta(Perfil perfil, OfertaLaboral oferta);
}