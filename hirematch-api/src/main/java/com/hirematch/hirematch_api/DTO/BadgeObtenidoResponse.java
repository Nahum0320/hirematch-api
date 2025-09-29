package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BadgeObtenidoResponse {
    private List<BadgeResponse> nuevosBadges;
    private String mensaje;
    private Boolean mostrarNotificacion;
    private LocalDateTime fechaObtenido;
    private Integer puntosGanados; // Puntos por obtener el badge
    private Boolean subirNivel; // Si subió de nivel con este badge
    private Integer nivelAnterior;
    private Integer nivelNuevo;
    private List<String> mensajesMotivacionales;
}