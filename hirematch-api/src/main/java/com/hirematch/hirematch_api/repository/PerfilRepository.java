package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    List<Perfil> findByUsuario_NombreContainingIgnoreCase(String nombreUsuario);
}
