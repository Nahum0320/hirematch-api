package com.hirematch.hirematch_api.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchEmpresaResponse {
    private Long postulacionId;
    private Long ofertaId;
    private String tituloOferta;
    private String descripcionOferta;
    private String ubicacionOferta;
    private LocalDateTime fechaPostulacion;
    private String estado;
    private boolean superLike;
    // Datos del usuario postulante
    private Long usuarioId;
    private String nombreUsuario;
    private String apellidoUsuario;
    private String emailUsuario;
    private String fotoUrl;
}
