package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.FotoPerfil;
import com.hirematch.hirematch_api.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FotoPerfilRepository extends JpaRepository<FotoPerfil, Long> {
    Optional<FotoPerfil> findByPerfil(Perfil perfil);
}