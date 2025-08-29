package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfertaResponse {

    private Long id;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private String empresaNombre;

}
