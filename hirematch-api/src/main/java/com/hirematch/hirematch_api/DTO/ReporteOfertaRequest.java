package com.hirematch.hirematch_api.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReporteOfertaRequest {

    @NotNull(message = "El id de la oferta es obligatorio")
    private Long ofertaId;

    @NotBlank(message = "El tipo de reporte es obligatorio")
    private String tipo;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    public Long getOfertaId() {
        return ofertaId;
    }

    public void setOfertaId(Long ofertaId) {
        this.ofertaId = ofertaId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
