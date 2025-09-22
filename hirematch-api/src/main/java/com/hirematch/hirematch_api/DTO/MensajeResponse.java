package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeResponse {
    private Long id;
    private Long chatId;
    private Long remitenteId;
    private String contenido;
    private LocalDateTime fechaEnvio;
}
