package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfertaResponse {

    // === INFORMACIÓN BÁSICA ===
    private Long id;
    private String titulo;
    private String descripcion;
    private String ubicacion;

    // === EMPRESA ===
    private Long empresaId;
    private String empresaNombre;
    private String empresaDescripcion;
    private String empresaSitioWeb;

    // === DETALLES DEL TRABAJO ===
    private String tipoTrabajo;
    private String tipoTrabajoDescripcion;
    private String tipoContrato;
    private String tipoContratoDescripcion;
    private String nivelExperiencia;
    private String nivelExperienciaDescripcion;
    private String areaTrabajo;

    // === COMPENSACIÓN ===
    private BigDecimal salarioMinimo;
    private BigDecimal salarioMaximo;
    private String moneda;
    private String monedaSimbolo;
    private Boolean salarioNegociable;
    private Boolean mostrarSalario;
    private String salarioFormateado; // "₡500,000 - ₡800,000" o "Salario negociable"

    // === BENEFICIOS Y REQUISITOS ===
    private String beneficios;
    private String requisitos;
    private String habilidadesRequeridas;
    private String idiomas;

    // === FECHAS Y ESTADO ===
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaCierre;
    private LocalDateTime fechaActualizacion;
    private String estado;
    private String estadoDescripcion;

    // === CONFIGURACIÓN ===
    private Integer vacantesDisponibles;
    private Boolean aplicacionRapida;
    private String preguntasAdicionales;

    // === INFORMACIÓN ADICIONAL PARA UI ===
    private Boolean urgente;
    private Boolean destacada;
    private List<String> etiquetas;
    private Boolean permiteAplicacionExterna;
    private String urlAplicacionExterna;

    // === ESTADÍSTICAS ===
    private Integer vistas;
    private Integer aplicacionesRecibidas;

    // === CAMPOS CALCULADOS PARA UI ===
    private Boolean isActiva;
    private Boolean isPausada;
    private Boolean isCerrada;
    private Boolean isExpirada;
    private String tiempoPublicacion; // "Hace 2 días", "Hace 1 semana"
    private Integer diasParaCierre; // Días restantes para que cierre
}