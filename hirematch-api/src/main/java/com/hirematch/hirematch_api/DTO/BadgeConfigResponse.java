package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BadgeConfigResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String icono;
    private String color;
    private String tipo;
    private Integer condicionRequerida;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private Long totalUsuariosConBadge;
    private Double porcentajeUsuarios;
    private List<UsuarioConBadgeResponse> usuariosRecientes; // Últimos que lo obtuvieron
}