package com.hirematch.hirematch_api.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReporteUsuarioRequest {
    private Long reportadoId;
    private Long ofertaId;
    @NotBlank(message = "El tipo de reporte es obligatorio")
    private String tipo;
    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
}