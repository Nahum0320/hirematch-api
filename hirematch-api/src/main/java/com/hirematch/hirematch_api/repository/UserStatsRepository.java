package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    Optional<UserStats> findByUsuario(Usuario usuario);
    Optional<UserStats> findByUsuarioUsuarioId(Long usuarioId);
}