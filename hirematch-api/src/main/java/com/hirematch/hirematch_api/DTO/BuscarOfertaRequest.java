package com.hirematch.hirematch_api.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuscarOfertaRequest {
    
    // === BÚSQUEDA DE TEXTO ===
    private String textoBusqueda; // Búsqueda libre en título y descripción
    
    // === FILTROS EXISTENTES ===
    private String ubicacion;
    private String tipoTrabajo;
    private String nivelExperiencia;
    private String areaTrabajo;
    
    // === FILTROS ADICIONALES ===
    private Long empresaId;
    private Boolean urgente;
    private Boolean destacada;
    private java.math.BigDecimal salarioMinimo;
    private java.math.BigDecimal salarioMaximo;
    
    // === FILTROS DE FECHA ===
    private Integer diasRecientes; // Solo ofertas de los últimos N días
    
    // === PAGINACIÓN ===
    private Integer size = 20;
    private Integer page = 0;
    
    // === ORDENAMIENTO ===
    private String ordenarPor = "relevancia"; // relevancia, fecha, salario, vistas
    private String direccionOrden = "desc"; // asc, desc
}

