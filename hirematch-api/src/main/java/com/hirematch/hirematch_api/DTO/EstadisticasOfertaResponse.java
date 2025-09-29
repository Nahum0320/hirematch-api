package com.hirematch.hirematch_api.DTO;

import com.hirematch.hirematch_api.entity.EstadoOferta;
import com.hirematch.hirematch_api.entity.NivelExperiencia;
import com.hirematch.hirematch_api.entity.TipoContrato;
import com.hirematch.hirematch_api.entity.TipoTrabajo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasOfertaResponse {
    // Información básica de la oferta
    private Long ofertaId;
    private String titulo;
    private String descripcion;

    // Estadísticas de postulaciones
    private Integer totalPostulaciones;
    private Integer totalMatches;
    private Integer totalSuperlikes;
    private Integer totalRechazosEmpresa;
    private Integer totalRechazosPostulante;
    private Integer totalContactados;
    private Integer vistasOferta;
    private Integer vacantesDisponibles;
    private EstadoOferta estadoOferta;
    private NivelExperiencia nivelExperiencia;
    private TipoTrabajo tipoTrabajo;
    private TipoContrato tipoContrato;

    // Estadísticas de actividad
    private Integer diasActiva;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Estadísticas calculadas
    private Double tasaAceptacion; // matches / postulaciones
    private Double tasaRechazoEmpresa; // rechazos de la empresa / matches
    private Double tasaRechazo; // rechazos del usuario / postulaciones
    private Double tasaContacto; // contactados / matches
}