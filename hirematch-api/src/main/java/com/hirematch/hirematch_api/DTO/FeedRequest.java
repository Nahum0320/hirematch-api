package com.hirematch.hirematch_api.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedRequest {
    
    // Filtros opcionales
    private String ubicacion;
    private String tipoTrabajo;
    private String nivelExperiencia;
    private String areaTrabajo;
    
    // Filtros adicionales que se pueden agregar en el futuro
    private Long empresaId;
    private Boolean urgente;
    private Boolean destacada;
    private java.math.BigDecimal salarioMinimo;
    private java.math.BigDecimal salarioMaximo;
    
    // Paginación (se establece desde los parámetros de query)
    private Integer size = 20;
    private Integer page = 0;
}
