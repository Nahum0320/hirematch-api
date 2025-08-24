package com.hirematch.hirematch_api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hirematch.hirematch_api.entity.EstadoSesion;
import com.hirematch.hirematch_api.entity.Sesion;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {

    @Query("SELECT s FROM Sesion s WHERE s.usuario.usuarioId = :usuarioId")
    Optional<Sesion> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    List<Sesion> findByEstado(EstadoSesion estado);

    @Query("SELECT s FROM Sesion s WHERE s.usuario.usuarioId = :usuarioId AND s.estado = :estado")
    Optional<Sesion> findByUsuarioIdAndEstado(@Param("usuarioId") Long usuarioId, @Param("estado") EstadoSesion estado);

    // Buscar sesiones activas que han expirado
    @Query("SELECT s FROM Sesion s WHERE s.estado = 'ACTIVA' AND s.fechaFin < :fechaActual")
    List<Sesion> findBySesionesActivasExpiradas(@Param("fechaActual") LocalDateTime fechaActual);
    
    // Verificar si un usuario tiene sesiones activas
    @Query("SELECT COUNT(s) > 0 FROM Sesion s WHERE s.usuario.usuarioId = :usuarioId AND s.estado = 'ACTIVA' AND s.fechaFin > :fechaActual")
    boolean hasActiveSessions(@Param("usuarioId") Long usuarioId, @Param("fechaActual") LocalDateTime fechaActual);

}
