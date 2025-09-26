package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActividadRecienteResponse {
    private String tipo; // "MATCH", "LIKE", "SUPERLIKE", "BADGE_OBTENIDO"
    private String descripcion;
    private LocalDateTime fecha;
    private String icono;
    private String color;
    private String tiempoTranscurrido; // "Hace 2 horas"
    private Object datosAdicionales; // Información específica según el tipo
}