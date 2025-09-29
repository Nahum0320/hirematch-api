package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioConBadgeResponse {
    private Long perfilId;
    private String nombreCompleto;
    private String email;
    private String fotoUrl;
    private LocalDateTime fechaObtenido;
    private String tiempoTranscurrido;
}