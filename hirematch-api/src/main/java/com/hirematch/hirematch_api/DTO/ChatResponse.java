package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private Long id;
    private Long ofertaId;
    private String tituloOferta;
    private String nombreContraparte; // Nombre del otro usuario (postulante o empresa)
    private String ultimoMensaje;
    private LocalDateTime ultimaActividad;
    private Long noLeidos; // Cantidad de mensajes no leídos
}
