package com.hirematch.hirematch_api.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CrearOfertaRequest {

    private Long empresaId; // Opcional si se obtiene del usuario autenticado

    // === INFORMACIÓN BÁSICA ===
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede exceder 150 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String descripcion;

    @Size(max = 150, message = "La ubicación no puede exceder 150 caracteres")
    private String ubicacion;

    // === DETALLES DEL TRABAJO ===
    private String tipoTrabajo; // REMOTO, PRESENCIAL, HIBRIDO
    private String tipoContrato; // TIEMPO_COMPLETO, MEDIO_TIEMPO, etc.
    private String nivelExperiencia; // JUNIOR, SENIOR, etc.

    @Size(max = 100, message = "El área no puede exceder 100 caracteres")
    private String areaTrabajo;

    // === COMPENSACIÓN ===
    @DecimalMin(value = "0.0", message = "El salario mínimo debe ser mayor a 0")
    private BigDecimal salarioMinimo;

    @DecimalMin(value = "0.0", message = "El salario máximo debe ser mayor a 0")
    private BigDecimal salarioMaximo;

    private String moneda; // USD, CRC, etc.
    private Boolean salarioNegociable = false;
    private Boolean mostrarSalario = true;

    // === BENEFICIOS Y REQUISITOS ===
    @Size(max = 1500, message = "Los beneficios no pueden exceder 1500 caracteres")
    private String beneficios;

    @Size(max = 1500, message = "Los requisitos no pueden exceder 1500 caracteres")
    private String requisitos;

    @Size(max = 1000, message = "Las habilidades no pueden exceder 1000 caracteres")
    private String habilidadesRequeridas;

    @Size(max = 500, message = "Los idiomas no pueden exceder 500 caracteres")
    private String idiomas;

    // === CONFIGURACIÓN ===
    private LocalDateTime fechaCierre;

    @Min(value = 1, message = "Debe haber al menos 1 vacante disponible")
    private Integer vacantesDisponibles = 1;

    private Boolean aplicacionRapida = true;

    @Size(max = 1000, message = "Las preguntas adicionales no pueden exceder 1000 caracteres")
    private String preguntasAdicionales;

    private Boolean urgente = false;
    private Boolean destacada = false;

    @Size(max = 200, message = "Las etiquetas no pueden exceder 200 caracteres")
    private String etiquetas;

    private Boolean permiteAplicacionExterna = false;

    @Size(max = 500, message = "La URL externa no puede exceder 500 caracteres")
    private String urlAplicacionExterna;
}