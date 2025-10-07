package com.hirematch.hirematch_api.repository;
import com.hirematch.hirematch_api.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hirematch.hirematch_api.entity.OfertaLaboral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface OfertaLaboralRepository extends JpaRepository<OfertaLaboral, Long> {

    // === BÚSQUEDAS BÁSICAS ===
    
    
    // Obtener ofertas activas ordenadas por fecha de publicación
    Page<OfertaLaboral> findByEstadoOrderByFechaPublicacionDesc(EstadoOferta estado, Pageable pageable);

    // Obtener ofertas por empresa
    Page<OfertaLaboral> findByEmpresaOrderByFechaPublicacionDesc(Empresa empresa, Pageable pageable);
    Page<OfertaLaboral> findByEmpresa_EmpresaId(Long empresaId, Pageable pageable);
    // Obtener ofertas por estado
    List<OfertaLaboral> findByEstado(EstadoOferta estado);



    // === BÚSQUEDAS AVANZADAS PARA MATCHING ===

    // Buscar por ubicación (contiene)
    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND (:ubicacion IS NULL OR LOWER(o.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%'))) " +
            "ORDER BY o.fechaPublicacion DESC")
    Page<OfertaLaboral> findByEstadoAndUbicacionContaining(@Param("estado") EstadoOferta estado,
                                                           @Param("ubicacion") String ubicacion,
                                                           Pageable pageable);

    // Buscar por tipo de trabajo
    Page<OfertaLaboral> findByEstadoAndTipoTrabajoOrderByFechaPublicacionDesc(EstadoOferta estado,
                                                                              TipoTrabajo tipoTrabajo,
                                                                              Pageable pageable);

    // Buscar por nivel de experiencia
    Page<OfertaLaboral> findByEstadoAndNivelExperienciaOrderByFechaPublicacionDesc(EstadoOferta estado,
                                                                                   NivelExperiencia nivelExperiencia,
                                                                                   Pageable pageable);

    // Buscar por área de trabajo
    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND (:areaTrabajo IS NULL OR LOWER(o.areaTrabajo) LIKE LOWER(CONCAT('%', :areaTrabajo, '%'))) " +
            "ORDER BY o.fechaPublicacion DESC")
    Page<OfertaLaboral> findByEstadoAndAreaTrabajoContaining(@Param("estado") EstadoOferta estado,
                                                             @Param("areaTrabajo") String areaTrabajo,
                                                             Pageable pageable);

    // === BÚSQUEDAS CON MÚLTIPLES FILTROS ===

    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND (:tipoTrabajo IS NULL OR o.tipoTrabajo = :tipoTrabajo) " +
            "AND (:nivelExperiencia IS NULL OR o.nivelExperiencia = :nivelExperiencia) " +
            "AND (:ubicacion IS NULL OR LOWER(o.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%'))) " +
            "AND (:areaTrabajo IS NULL OR LOWER(o.areaTrabajo) LIKE LOWER(CONCAT('%', :areaTrabajo, '%'))) " +
            "ORDER BY " +
            "CASE WHEN o.destacada = true THEN 0 ELSE 1 END, " +
            "CASE WHEN o.urgente = true THEN 0 ELSE 1 END, " +
            "o.fechaPublicacion DESC")
    Page<OfertaLaboral> findWithFilters(@Param("estado") EstadoOferta estado,
                                        @Param("tipoTrabajo") TipoTrabajo tipoTrabajo,
                                        @Param("nivelExperiencia") NivelExperiencia nivelExperiencia,
                                        @Param("ubicacion") String ubicacion,
                                        @Param("areaTrabajo") String areaTrabajo,
                                        Pageable pageable);

    // === BÚSQUEDAS POR RANGO SALARIAL ===

    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND o.mostrarSalario = true " +
            "AND ((:salarioMin IS NULL OR o.salarioMaximo IS NULL OR o.salarioMaximo >= :salarioMin) " +
            "AND (:salarioMax IS NULL OR o.salarioMinimo IS NULL OR o.salarioMinimo <= :salarioMax)) " +
            "ORDER BY o.fechaPublicacion DESC")
    Page<OfertaLaboral> findByEstadoAndSalarioRange(@Param("estado") EstadoOferta estado,
                                                    @Param("salarioMin") java.math.BigDecimal salarioMin,
                                                    @Param("salarioMax") java.math.BigDecimal salarioMax,
                                                    Pageable pageable);

    // === BÚSQUEDAS POR HABILIDADES ===

    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND (:habilidad IS NULL OR LOWER(o.habilidadesRequeridas) LIKE LOWER(CONCAT('%', :habilidad, '%'))) " +
            "ORDER BY o.fechaPublicacion DESC")
    Page<OfertaLaboral> findByEstadoAndHabilidadesContaining(@Param("estado") EstadoOferta estado,
                                                             @Param("habilidad") String habilidad,
                                                             Pageable pageable);

    // === BÚSQUEDAS POR FECHA ===

    // Ofertas publicadas en los últimos N días
    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND o.fechaPublicacion >= :fechaDesde " +
            "ORDER BY o.fechaPublicacion DESC")
    Page<OfertaLaboral> findRecentOffers(@Param("estado") EstadoOferta estado,
                                         @Param("fechaDesde") LocalDateTime fechaDesde,
                                         Pageable pageable);

    // Ofertas que cierran pronto (próximos N días)
    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND o.fechaCierre IS NOT NULL " +
            "AND o.fechaCierre BETWEEN :fechaActual AND :fechaLimite " +
            "ORDER BY o.fechaCierre ASC")
    Page<OfertaLaboral> findClosingSoon(@Param("estado") EstadoOferta estado,
                                        @Param("fechaActual") LocalDateTime fechaActual,
                                        @Param("fechaLimite") LocalDateTime fechaLimite,
                                        Pageable pageable);

    // === OFERTAS DESTACADAS Y URGENTES ===

    // Ofertas destacadas
    Page<OfertaLaboral> findByEstadoAndDestacadaTrueOrderByFechaPublicacionDesc(EstadoOferta estado, Pageable pageable);

    // Ofertas urgentes
    Page<OfertaLaboral> findByEstadoAndUrgenteTrueOrderByFechaPublicacionDesc(EstadoOferta estado, Pageable pageable);

    // Ofertas destacadas o urgentes (feed premium)
    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND (o.destacada = true OR o.urgente = true) " +
            "ORDER BY " +
            "CASE WHEN o.destacada = true THEN 0 ELSE 1 END, " +
            "CASE WHEN o.urgente = true THEN 0 ELSE 1 END, " +
            "o.fechaPublicacion DESC")
    Page<OfertaLaboral> findPremiumOffers(@Param("estado") EstadoOferta estado, Pageable pageable);

    // === ESTADÍSTICAS Y BÚSQUEDAS ADMINISTRATIVAS ===

    // Ofertas más vistas
    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "ORDER BY o.vistas DESC, o.fechaPublicacion DESC")
    Page<OfertaLaboral> findMostViewed(@Param("estado") EstadoOferta estado, Pageable pageable);

    // Ofertas con más aplicaciones
    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "ORDER BY o.aplicacionesRecibidas DESC, o.fechaPublicacion DESC")
    Page<OfertaLaboral> findMostApplied(@Param("estado") EstadoOferta estado, Pageable pageable);

    // === BÚSQUEDAS POR EMPRESA ===

    // Ofertas activas de una empresa
    Page<OfertaLaboral> findByEmpresaAndEstadoOrderByFechaPublicacionDesc(Empresa empresa,
                                                                          EstadoOferta estado,
                                                                          Pageable pageable);

    // Contar ofertas por empresa y estado
    Long countByEmpresaAndEstado(Empresa empresa, EstadoOferta estado);

    // Ofertas por usuario (a través de empresa)
    @Query("SELECT o FROM OfertaLaboral o WHERE o.empresa.usuario.usuarioId = :usuarioId " +
            "ORDER BY o.fechaPublicacion DESC")
    Page<OfertaLaboral> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // === BÚSQUEDAS DE TEXTO COMPLETO ===

    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND (:searchTerm IS NULL OR " +
            "LOWER(o.titulo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.descripcion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.habilidadesRequeridas) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.empresa.nombreEmpresa) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY " +
            "CASE WHEN o.destacada = true THEN 0 ELSE 1 END, " +
            "CASE WHEN o.urgente = true THEN 0 ELSE 1 END, " +
            "o.fechaPublicacion DESC")
    Page<OfertaLaboral> searchOffers(@Param("estado") EstadoOferta estado,
                                     @Param("searchTerm") String searchTerm,
                                     Pageable pageable);

    // === MANTENIMIENTO Y LIMPIEZA ===

    // Encontrar ofertas expiradas que aún están activas
    @Query("SELECT o FROM OfertaLaboral o WHERE o.estado = 'ACTIVA' " +
            "AND o.fechaCierre IS NOT NULL " +
            "AND o.fechaCierre < :fechaActual")
    List<OfertaLaboral> findExpiredActiveOffers(@Param("fechaActual") LocalDateTime fechaActual);

    // Ofertas antiguas sin actividad (para archivo)
    @Query("SELECT o FROM OfertaLaboral o WHERE o.fechaPublicacion < :fechaLimite " +
            "AND o.vistas < :vistasMiniimas")
    List<OfertaLaboral> findOldInactiveOffers(@Param("fechaLimite") LocalDateTime fechaLimite,
                                              @Param("vistasMiniimas") Integer vistasMiniimas);

    // === RECOMENDACIONES Y MATCHING AVANZADO ===

    @Query(
            value = "SELECT * FROM ofertas_laborales o " +
                    "WHERE o.estado = :estado " +
                    "AND (:ubicacionUsuario IS NULL OR LOWER(o.ubicacion) LIKE LOWER(CONCAT('%', :ubicacionUsuario, '%'))) " +
                    "AND (:tipoTrabajo IS NULL OR o.tipo_trabajo = :tipoTrabajo) " +
                    "AND (:nivelExperiencia IS NULL OR o.nivel_experiencia = :nivelExperiencia) " +
                    "AND (:areaTrabajo IS NULL OR LOWER(o.area_trabajo) LIKE LOWER(CONCAT('%', :areaTrabajo, '%'))) " +
                    "AND (:empresaId IS NULL OR o.empresa_id = :empresaId) " +
                    "AND (:urgente IS NULL OR o.urgente = :urgente) " +
                    "AND (:destacada IS NULL OR o.destacada = :destacada) " +
                    "AND (:salarioMinimo IS NULL OR o.salario_minimo >= :salarioMinimo) " +
                    "AND (:salarioMaximo IS NULL OR o.salario_maximo <= :salarioMaximo) " +
                    "AND o.oferta_id NOT IN ( " +
                    "   SELECT DISTINCT l.oferta_id FROM likes l " +
                    "   INNER JOIN perfiles p ON l.perfil_id = p.perfil_id " +
                    "   WHERE p.usuario_id = :usuarioId " +
                    "   UNION " +
                    "   SELECT DISTINCT pass.oferta_id FROM passes pass " +
                    "   INNER JOIN perfiles p ON pass.perfil_id = p.perfil_id " +
                    "   WHERE p.usuario_id = :usuarioId " +
                    "   UNION " +
                    "   SELECT DISTINCT po.oferta_id FROM postulantes_por_oferta po " +
                    "   INNER JOIN perfiles p ON po.postulante_id = p.perfil_id " +
                    "   WHERE p.usuario_id = :usuarioId " +
                    ") " +
                    "ORDER BY CASE WHEN o.destacada = true THEN 0 ELSE 1 END, " +
                    "CASE WHEN o.urgente = true THEN 0 ELSE 1 END, " +
                    "o.fecha_publicacion DESC",
            nativeQuery = true
    )
    Page<OfertaLaboral> findMatchingOffers(
            @Param("estado") String estado,
            @Param("ubicacionUsuario") String ubicacionUsuario,
            @Param("tipoTrabajo") String tipoTrabajo,
            @Param("nivelExperiencia") String nivelExperiencia,
            @Param("areaTrabajo") String areaTrabajo,
            @Param("empresaId") Long empresaId,
            @Param("urgente") Boolean urgente,
            @Param("destacada") Boolean destacada,
            @Param("salarioMinimo") BigDecimal salarioMinimo,
            @Param("salarioMaximo") BigDecimal salarioMaximo,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM ofertas_laborales o " +
                    "WHERE o.estado = :estado " +
                    "AND (:ubicacionUsuario IS NULL OR LOWER(o.ubicacion) LIKE LOWER(CONCAT('%', :ubicacionUsuario, '%'))) " +
                    "AND (:habilidadesUsuario IS NULL OR EXISTS ( " +
                    "   SELECT 1 FROM UNNEST(string_to_array(:habilidadesUsuario, ',')) AS skill " +
                    "   WHERE LOWER(o.habilidades_requeridas) LIKE CONCAT('%', LOWER(skill), '%')" +
                    ")) " +
                    "ORDER BY CASE WHEN o.destacada = true THEN 0 ELSE 1 END, " +
                    "CASE WHEN LOWER(o.ubicacion) LIKE LOWER(CONCAT('%', :ubicacionUsuario, '%')) THEN 0 ELSE 1 END, " +
                    "o.fecha_publicacion DESC",
            nativeQuery = true
    )
    Page<OfertaLaboral> findMatchingOffersWithoutUser(
            @Param("estado") String estado,
            @Param("ubicacionUsuario") String ubicacionUsuario,
            @Param("habilidadesUsuario") String habilidadesUsuario,
            Pageable pageable
    );


    // === CONSULTAS DE AGREGACIÓN ===

    // Contar ofertas por tipo de trabajo
    @Query("SELECT o.tipoTrabajo, COUNT(o) FROM OfertaLaboral o WHERE o.estado = :estado GROUP BY o.tipoTrabajo")
    List<Object[]> countByTipoTrabajo(@Param("estado") EstadoOferta estado);

    // Contar ofertas por nivel de experiencia
    @Query("SELECT o.nivelExperiencia, COUNT(o) FROM OfertaLaboral o WHERE o.estado = :estado GROUP BY o.nivelExperiencia")
    List<Object[]> countByNivelExperiencia(@Param("estado") EstadoOferta estado);

    // Estadísticas de salario promedio por área
    @Query("SELECT o.areaTrabajo, AVG(o.salarioMinimo), AVG(o.salarioMaximo), COUNT(o) " +
            "FROM OfertaLaboral o WHERE o.estado = :estado " +
            "AND o.salarioMinimo IS NOT NULL " +
            "GROUP BY o.areaTrabajo")
    List<Object[]> getSalaryStatsByArea(@Param("estado") EstadoOferta estado);
}