package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioBadgeResponse {
    private Long id;
    private BadgeResponse badge;
    private LocalDateTime fechaObtenido;
    private Integer progresoActual;
    private Boolean esNuevo; // Para mostrar animación de nuevo badge
    private Boolean notificado;
    private String tiempoTranscurrido; // "Hace 2 días", etc.
}
