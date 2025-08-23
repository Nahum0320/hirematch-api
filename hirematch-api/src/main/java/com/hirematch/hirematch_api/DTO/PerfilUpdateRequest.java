package com.hirematch.hirematch_api.DTO;

import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
public class PerfilUpdateRequest {

    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String descripcion;

    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres")
    private String ubicacion;

    @Size(max = 500, message = "Las habilidades no pueden exceder los 500 caracteres")
    private String habilidades;

    @Pattern(regexp = "^$|^[+\\d\\s\\-()]+$", message = "El teléfono solo puede contener números, espacios, guiones, paréntesis y el símbolo +")
    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;

    @Pattern(regexp = "^$|^(https?://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/.*)?$", message = "El formato de la URL no es válido")
    @Size(max = 255, message = "El sitio web no puede exceder los 255 caracteres")
    private String sitioWeb;

    @Size(max = 2000, message = "La experiencia no puede exceder los 2000 caracteres")
    private String experiencia;

    @Size(max = 2000, message = "La educación no puede exceder los 2000 caracteres")
    private String educacion;

    @Size(max = 2000, message = "Las certificaciones no pueden exceder los 2000 caracteres")
    private String certificaciones;

    @Size(max = 500, message = "Los intereses no pueden exceder los 500 caracteres")
    private String intereses;
}