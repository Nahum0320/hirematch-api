package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.Badge;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.entity.UsuarioBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioBadgeRepository extends JpaRepository<UsuarioBadge, Long> {

    List<UsuarioBadge> findByPerfilOrderByFechaObtenidoDesc(Perfil perfil);

    Optional<UsuarioBadge> findByPerfilAndBadge(Perfil perfil, Badge badge);

    boolean existsByPerfilAndBadge(Perfil perfil, Badge badge);

    @Query("SELECT ub FROM UsuarioBadge ub JOIN FETCH ub.badge WHERE ub.perfil = :perfil ORDER BY ub.fechaObtenido DESC")
    List<UsuarioBadge> findByPerfilWithBadgeDetails(@Param("perfil") Perfil perfil);

    List<UsuarioBadge> findByNotificadoFalse();

    @Query("SELECT COUNT(ub) FROM UsuarioBadge ub WHERE ub.perfil = :perfil")
    Long countBadgesByPerfil(@Param("perfil") Perfil perfil);

    @Query("SELECT ub FROM UsuarioBadge ub JOIN FETCH ub.badge WHERE ub.perfil = :perfil AND ub.badge.tipo = :tipo ORDER BY ub.fechaObtenido DESC")
    List<UsuarioBadge> findByPerfilAndBadgeType(@Param("perfil") Perfil perfil, @Param("tipo") Badge.TipoBadge tipo);

    @Query("SELECT ub FROM UsuarioBadge ub JOIN FETCH ub.badge WHERE ub.fechaObtenido >= :fechaInicio ORDER BY ub.fechaObtenido DESC")
    List<UsuarioBadge> findRecentBadges(@Param("fechaInicio") LocalDateTime fechaInicio);

    @Query("SELECT ub FROM UsuarioBadge ub JOIN FETCH ub.badge WHERE ub.perfil = :perfil AND ub.fechaObtenido >= :fechaInicio ORDER BY ub.fechaObtenido DESC")
    List<UsuarioBadge> findRecentBadgesByPerfil(@Param("perfil") Perfil perfil, @Param("fechaInicio") LocalDateTime fechaInicio);

    @Modifying
    @Transactional
    @Query("UPDATE UsuarioBadge ub SET ub.notificado = true WHERE ub.perfil = :perfil AND ub.notificado = false")
    void marcarBadgesComoNotificados(@Param("perfil") Perfil perfil);

    @Query("SELECT ub FROM UsuarioBadge ub JOIN FETCH ub.badge WHERE ub.perfil = :perfil AND ub.notificado = false")
    List<UsuarioBadge> findBadgesNoNotificadosByPerfil(@Param("perfil") Perfil perfil);

    @Query("SELECT COUNT(ub) FROM UsuarioBadge ub WHERE ub.badge.tipo = :tipo")
    Long countUsuariosConBadgeTipo(@Param("tipo") Badge.TipoBadge tipo);

    // Top usuarios con más badges
    @Query("SELECT ub.perfil, COUNT(ub) as total FROM UsuarioBadge ub GROUP BY ub.perfil ORDER BY total DESC")
    List<Object[]> findTopUsuariosByBadges();
}