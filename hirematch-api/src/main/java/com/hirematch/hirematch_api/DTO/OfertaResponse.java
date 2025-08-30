package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfertaResponse {


    private Long id;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private String empresaNombre;

}
