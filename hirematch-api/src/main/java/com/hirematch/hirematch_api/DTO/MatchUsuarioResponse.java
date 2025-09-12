package com.hirematch.hirematch_api.DTO;

import com.hirematch.hirematch_api.entity.EstadoPostulacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchUsuarioResponse {
    private Long postulacionId;
    private Long ofertaId;
    private String tituloOferta;
    private String descripcionOferta;
    private String ubicacionOferta;
    private String empresaNombre;
    private String empresaDescripcion;
    private LocalDateTime fechaPostulacion;
    private String estado;
    private boolean superLike;
}
