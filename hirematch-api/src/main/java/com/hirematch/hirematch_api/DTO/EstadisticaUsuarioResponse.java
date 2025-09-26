package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaUsuarioResponse {
    private Long perfilId;
    
    // Estadísticas de matches
    private Integer totalMatches;
    private Integer totalLikesDados;
    private Integer totalLikesRecibidos;
    private Integer totalSuperlikesDados;
    private Integer totalSuperlikesRecibidos;
    private Integer totalRechazosDados;
    private Integer totalRechazosRecibidos;
    
    // Estadísticas de perfil
    private Boolean perfilCompletado;
    private Integer porcentajePerfil;
    
    // Estadísticas de actividad
    private Integer diasActivo;
    private LocalDateTime ultimaActividad;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    
    // Estadísticas calculadas
    private Double tasaExito; // matches / likes dados
    private Double popularidad; // likes recibidos / total de interacciones
    private Double tasaRespuesta; // (likes recibidos + matches) / total likes dados
    private Integer totalBadges; // Cantidad de badges obtenidos
    
    // Comparaciones con promedios
    private String rendimientoVsPromedio; // "Por encima del promedio", etc.
    private Double posicionRanking; // Percentil del usuario
}