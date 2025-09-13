package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByUsuario(Usuario usuario);

    // Nuevo método para obtener perfil por email del usuario
    @Query("SELECT p FROM Perfil p WHERE p.usuario.email = :email")
    Optional<Perfil> findByUsuarioEmail(@Param("email") String email);
}


