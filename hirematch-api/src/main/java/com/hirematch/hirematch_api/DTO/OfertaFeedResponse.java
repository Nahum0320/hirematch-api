package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfertaFeedResponse {

    private Long id;
    private String titulo;
    private String descripcion;
    private String ubicacion;

    // Empresa
    private String empresaNombre;
    private String empresaDescripcion;

    // Detalles básicos
    private String tipoTrabajo;
    private String tipoContrato;
    private String nivelExperiencia;
    private String areaTrabajo;

    // Salario
    private String salarioFormateado;
    private Boolean mostrarSalario;

    // Fechas
    private LocalDateTime fechaPublicacion;
    private String tiempoPublicacion;
    private Integer diasParaCierre;

    // UI/UX
    private Boolean urgente;
    private Boolean destacada;
    private List<String> etiquetas;
    private Boolean aplicacionRapida;

    // Estadísticas
    private Integer vistas;
    private Integer aplicacionesRecibidas;

    // Estado
    private Boolean isActiva;
}