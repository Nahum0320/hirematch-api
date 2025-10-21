package com.hirematch.hirematch_api.DTO;

import com.hirematch.hirematch_api.entity.EstadoReporte;
import com.hirematch.hirematch_api.entity.TipoReporte;

import java.time.LocalDateTime;

public class ReporteAdminResponse {
    public Long id;
    public UsuarioSummary reportante;
    public UsuarioSummary reportado; // puede ser null
    public Long ofertaId; // puede ser null
    public TipoReporte tipo;
    public String motivo;
    public EstadoReporte estado;
    public LocalDateTime fecha;

    public ReporteAdminResponse(Long id, UsuarioSummary reportante, UsuarioSummary reportado, Long ofertaId, TipoReporte tipo, String motivo, EstadoReporte estado, LocalDateTime fecha) {
        this.id = id;
        this.reportante = reportante;
        this.reportado = reportado;
        this.ofertaId = ofertaId;
        this.tipo = tipo;
        this.motivo = motivo;
        this.estado = estado;
        this.fecha = fecha;
    }
}
