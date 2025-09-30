package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasEmpresaResponse {
    // Información básica de la empresa
    private Long empresaId;
    private String nombreEmpresa;
    private String descripcion;
    private String sitioWeb;
    
    // Estadísticas de ofertas
    private Integer totalOfertas;
    private Integer ofertasActivas;
    private Integer ofertasInactivas;
    private Integer ofertasDestacadas;
    
    // Estadísticas agregadas de todas las ofertas
    private Integer totalPostulaciones;
    private Integer totalMatches;
    private Integer totalSuperlikes;
    private Integer totalRechazosEmpresa;
    private Integer totalRechazosPostulante;
    private Integer totalContactados;
    private Integer totalVistasOfertas;
    private Integer totalVacantesDisponibles;
    
    // Estadísticas de actividad
    private Integer diasActiva;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimaActividad;
    
    // Estadísticas calculadas
    private Double tasaAceptacion; // matches / postulaciones totales
    private Double tasaRechazoEmpresa; // rechazos de la empresa / matches totales
    private Double tasaRechazo; // rechazos del postulante / total interacciones
    private Double tasaContacto; // contactados / matches totales
    
    // Estadísticas de perfil de empresa
    private Boolean perfilCompletado;
    private Integer porcentajePerfil;
    
    // Estadísticas de engagement
    private Double indiceEngagement; // (matches + contactados) / total vistas
    private Integer totalBadges; // Cantidad de badges obtenidos
}

