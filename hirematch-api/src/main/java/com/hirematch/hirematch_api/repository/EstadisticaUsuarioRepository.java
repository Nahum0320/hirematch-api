package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.EstadisticaUsuario;
import com.hirematch.hirematch_api.entity.Perfil;
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
public interface EstadisticaUsuarioRepository extends JpaRepository<EstadisticaUsuario, Long> {

    Optional<EstadisticaUsuario> findByPerfil(Perfil perfil);

    // Métodos para incrementar estadísticas
    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.totalMatches = e.totalMatches + 1, e.ultimaActividad = :fecha WHERE e.perfil = :perfil")
    void incrementarMatches(@Param("perfil") Perfil perfil, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.totalLikesDados = e.totalLikesDados + 1, e.ultimaActividad = :fecha WHERE e.perfil = :perfil")
    void incrementarLikesDados(@Param("perfil") Perfil perfil, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.totalLikesRecibidos = e.totalLikesRecibidos + 1 WHERE e.perfil = :perfil")
    void incrementarLikesRecibidos(@Param("perfil") Perfil perfil);

    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.totalSuperlikesDados = e.totalSuperlikesDados + 1, e.ultimaActividad = :fecha WHERE e.perfil = :perfil")
    void incrementarSuperlikesDados(@Param("perfil") Perfil perfil, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.totalSuperlikesRecibidos = e.totalSuperlikesRecibidos + 1 WHERE e.perfil = :perfil")
    void incrementarSuperlikesRecibidos(@Param("perfil") Perfil perfil);

    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.totalRechazosDados = e.totalRechazosDados + 1, e.ultimaActividad = :fecha WHERE e.perfil = :perfil")
    void incrementarRechazosDados(@Param("perfil") Perfil perfil, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.totalRechazosRecibidos = e.totalRechazosRecibidos + 1 WHERE e.perfil = :perfil")
    void incrementarRechazosRecibidos(@Param("perfil") Perfil perfil);

    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.ultimaActividad = :fecha WHERE e.perfil = :perfil")
    void actualizarUltimaActividad(@Param("perfil") Perfil perfil, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Transactional
    @Query("UPDATE EstadisticaUsuario e SET e.porcentajePerfil = :porcentaje, e.perfilCompletado = :completado WHERE e.perfil = :perfil")
    void actualizarPorcentajePerfil(@Param("perfil") Perfil perfil, @Param("porcentaje") Integer porcentaje, @Param("completado") Boolean completado);

    // Consultas estadísticas generales
    @Query("SELECT COUNT(e) FROM EstadisticaUsuario e WHERE e.totalMatches >= :minMatches")
    Long countUsuariosConMinMatches(@Param("minMatches") Integer minMatches);

    @Query("SELECT AVG(e.totalMatches) FROM EstadisticaUsuario e")
    Double getPromedioMatches();

    @Query("SELECT AVG(e.totalLikesDados) FROM EstadisticaUsuario e")
    Double getPromedioLikesDados();

    @Query("SELECT AVG(e.porcentajePerfil) FROM EstadisticaUsuario e")
    Double getPromedioPorcentajePerfil();

    @Query("SELECT COUNT(e) FROM EstadisticaUsuario e WHERE e.perfilCompletado = true")
    Long countPerfilesCompletados();

    @Query("SELECT COUNT(e) FROM EstadisticaUsuario e WHERE e.ultimaActividad >= :fecha")
    Long countUsuariosActivosDesde(@Param("fecha") LocalDateTime fecha);

    // Top usuarios por diferentes métricas
    @Query("SELECT e FROM EstadisticaUsuario e ORDER BY e.totalMatches DESC")
    List<EstadisticaUsuario> findTopByMatches();

    @Query("SELECT e FROM EstadisticaUsuario e WHERE e.totalLikesDados > 0 ORDER BY (CAST(e.totalMatches AS double) / e.totalLikesDados) DESC")
    List<EstadisticaUsuario> findTopByTasaExito();

    @Query("SELECT e FROM EstadisticaUsuario e ORDER BY e.totalLikesRecibidos DESC")
    List<EstadisticaUsuario> findTopByPopularidad();

    @Query("SELECT e FROM EstadisticaUsuario e WHERE e.ultimaActividad >= :fecha ORDER BY e.ultimaActividad DESC")
    List<EstadisticaUsuario> findUsuariosActivosRecientes(@Param("fecha") LocalDateTime fecha);

    // Consultas de rango de fechas
    @Query("SELECT COUNT(e) FROM EstadisticaUsuario e WHERE e.fechaRegistro BETWEEN :inicio AND :fin")
    Long countRegistrosEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT e FROM EstadisticaUsuario e WHERE e.fechaRegistro >= :fecha ORDER BY e.fechaRegistro DESC")
    List<EstadisticaUsuario> findUsuariosRegistradosDesde(@Param("fecha") LocalDateTime fecha);
}