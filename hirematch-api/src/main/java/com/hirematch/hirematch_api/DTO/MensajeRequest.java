package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeRequest {
    private Long ofertaId;
    private String contenido;
}