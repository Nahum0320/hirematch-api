package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BadgeDistribucionResponse {
    private String nombreBadge;
    private String icono;
    private String color;
    private Long cantidadUsuarios;
    private Double porcentajeUsuarios;
    private String rareza; // "COMÚN", "RARO", "ÉPICO", "LEGENDARIO"
}