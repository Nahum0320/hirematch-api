package com.hirematch.hirematch_api.repository;

import com.hirematch.hirematch_api.entity.EstadoReporte;
import com.hirematch.hirematch_api.entity.Reporte;
import com.hirematch.hirematch_api.entity.TipoReporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ReporteRepositoryCustom {
    Page<Reporte> buscarFiltros(EstadoReporte estado, TipoReporte tipo, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
