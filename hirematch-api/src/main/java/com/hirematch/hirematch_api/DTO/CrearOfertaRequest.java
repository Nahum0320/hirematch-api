package com.hirematch.hirematch_api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearOfertaRequest {

    private Long empresaId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255)
    private String descripcion;

    @Size(max = 150)
    private String ubicacion;

}
