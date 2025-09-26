package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogroDestacadoResponse {
    private String titulo;
    private String descripcion;
    private String icono;
    private String color;
    private Integer valor; // Número asociado al logro
    private String categoria; // "SOCIAL", "ACTIVIDAD", "PERFIL", etc.
    private Boolean esReciente; // Si fue obtenido recientemente
}