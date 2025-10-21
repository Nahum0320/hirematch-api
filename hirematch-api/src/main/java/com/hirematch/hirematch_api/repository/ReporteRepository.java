package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.EstadoReporte;
import com.hirematch.hirematch_api.entity.OfertaLaboral;
import com.hirematch.hirematch_api.entity.Reporte;
import com.hirematch.hirematch_api.entity.TipoReporte;
import com.hirematch.hirematch_api.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    // Consultas por estado y tipo
    Page<Reporte> findByEstado(EstadoReporte estado, Pageable pageable);
    Page<Reporte> findByTipo(TipoReporte tipo, Pageable pageable);

    // Filtros para administración
    Page<Reporte> findByReportante(Usuario reportante, Pageable pageable);
    Page<Reporte> findByReportado(Usuario reportado, Pageable pageable);
    Page<Reporte> findByOferta(OfertaLaboral oferta, Pageable pageable);
    Page<Reporte> findByEstadoAndTipo(EstadoReporte estado, TipoReporte tipo, Pageable pageable);

    // Estadísticas básicas
    long countByEstado(EstadoReporte estado);
    long countByTipo(TipoReporte tipo);

    @Query("SELECT r.tipo, COUNT(r) FROM Reporte r GROUP BY r.tipo")
    List<Object[]> getEstadisticasPorTipo();

    @Query("SELECT r.estado, COUNT(r) FROM Reporte r GROUP BY r.estado")
    List<Object[]> getEstadisticasPorEstado();

    @Query("SELECT COUNT(r) FROM Reporte r WHERE r.reportado = :reportado")
    long countByReportado(Usuario reportado);

    @Query("SELECT COUNT(r) FROM Reporte r WHERE r.oferta = :oferta")
    long countByOferta(OfertaLaboral oferta);

    // Búsqueda flexible para administración con filtros opcionales
    // Implementada en ReporteRepositoryImpl usando Criteria API para evitar problemas de tipos
    org.springframework.data.domain.Page<Reporte> buscarFiltros(
            EstadoReporte estado,
            TipoReporte tipo,
            java.time.LocalDateTime desde,
            java.time.LocalDateTime hasta,
            org.springframework.data.domain.Pageable pageable
    );

    // Prevención de duplicados para usuarios
    Optional<Reporte> findByReportanteAndReportadoAndTipoAndEstado(
            Usuario reportante, Usuario reportado, TipoReporte tipo, EstadoReporte estado
    );

    // Prevención de duplicados para ofertas
    Optional<Reporte> findByReportanteAndOfertaAndTipoAndEstado(
            Usuario reportante, OfertaLaboral oferta, TipoReporte tipo, EstadoReporte estado
    );
}