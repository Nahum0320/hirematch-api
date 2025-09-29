package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BadgeResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String icono;
    private String color;
    private String tipo;
    private String tipoDisplay;
    private Integer condicionRequerida;
    private LocalDateTime fechaObtenido; // Solo presente si el usuario lo tiene
    private Integer progresoActual; // Para badges con progreso
    private Boolean activo;
    private Boolean obtenido; // Indica si el usuario ya lo tiene
}
