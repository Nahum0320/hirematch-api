package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingUsuarioResponse {
    private Long perfilId;
    private String nombreCompleto;
    private String fotoUrl;
    private Integer posicion;
    private Integer totalBadges;
    private Integer nivelUsuario;
    private String titulo;
    private Double puntuacion; // Puntuación total del ranking
    private Boolean esUsuarioActual; // Si es el usuario que consulta
}