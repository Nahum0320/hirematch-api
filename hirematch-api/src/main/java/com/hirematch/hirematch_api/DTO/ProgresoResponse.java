package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgresoResponse {
    private String tipoBadge;
    private String nombreBadge;
    private String descripcion;
    private Integer progresoActual;
    private Integer progresoRequerido;
    private Double porcentajeCompletado;
    private String icono;
    private String color;
    private Boolean esSiguienteBadge; // Si es el próximo badge a obtener
    private Integer puntosRestantes; // Puntos que faltan para obtenerlo
    private String tiempoEstimado; // "Aproximadamente 3 días"
}