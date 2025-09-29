package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasGlobalesResponse {
    // Estadísticas generales
    private Long totalUsuarios;
    private Long totalMatches;
    private Long totalLikes;
    private Long totalSuperLikes;
    private Double promedioMatchesPorUsuario;
    private Double promedioLikesPorUsuario;
    private Double tasaExitoPromedio;
    
    // Distribución de badges
    private List<BadgeDistribucionResponse> distribucionBadges;
    
    // Rankings
    private List<RankingUsuarioResponse> topUsuariosPorBadges;
    private List<RankingUsuarioResponse> topUsuariosPorMatches;
    
    // Estadísticas de actividad
    private Long usuariosActivosUltimos7Dias;
    private Long usuariosActivosUltimos30Dias;
    private Long perfilesCompletados;
}