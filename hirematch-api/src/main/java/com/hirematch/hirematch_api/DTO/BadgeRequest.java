package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BadgeRequest {
    @NotBlank(message = "El nombre del badge es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotBlank(message = "El icono es obligatorio")
    @Size(max = 50, message = "El nombre del icono no puede exceder 50 caracteres")
    private String icono;
    
    @NotBlank(message = "El color es obligatorio")
    @Size(min = 7, max = 7, message = "El color debe ser un código hex válido (#RRGGBB)")
    private String color;
    
    @NotNull(message = "El tipo de badge es obligatorio")
    private String tipo;
    
    private Integer condicionRequerida;
    private Boolean activo = true;
}