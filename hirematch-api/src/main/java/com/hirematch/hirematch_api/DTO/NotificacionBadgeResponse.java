package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionBadgeResponse {
    private Long id;
    private BadgeResponse badge;
    private LocalDateTime fechaObtenido;
    private Boolean leida;
    private String mensaje;
    private String tipo; // "BADGE_NUEVO", "PROGRESO", "NIVEL_SUBIDO"
}