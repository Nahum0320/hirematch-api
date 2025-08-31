package com.hirematch.hirematch_api.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActualizarOfertaRequest {

    @Size(max = 150, message = "El título no puede exceder 150 caracteres")
    private String titulo;

    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String descripcion;

    @Size(max = 150, message = "La ubicación no puede exceder 150 caracteres")
    private String ubicacion;

    private String tipoTrabajo;
    private String tipoContrato;
    private String nivelExperiencia;
    private String areaTrabajo;

    @DecimalMin(value = "0.0", message = "El salario debe ser mayor a 0")
    private BigDecimal salarioMinimo;

    @DecimalMin(value = "0.0", message = "El salario debe ser mayor a 0")
    private BigDecimal salarioMaximo;

    private String moneda;
    private Boolean salarioNegociable;
    private Boolean mostrarSalario;

    private String beneficios;
    private String requisitos;
    private String habilidadesRequeridas;
    private String idiomas;

    private LocalDateTime fechaCierre;

    @Min(value = 1, message = "Debe haber al menos 1 vacante disponible")
    private Integer vacantesDisponibles;

    private Boolean aplicacionRapida;
    private String preguntasAdicionales;
    private Boolean urgente;
    private Boolean destacada;
    private String etiquetas;
    private Boolean permiteAplicacionExterna;
    private String urlAplicacionExterna;
    private String estado; // ACTIVA, PAUSADA, CERRADA
}
