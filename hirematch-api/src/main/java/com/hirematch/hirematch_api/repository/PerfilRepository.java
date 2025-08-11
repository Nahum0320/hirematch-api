package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNombreContainingIgnoreCase(String nombreUsario);
}

