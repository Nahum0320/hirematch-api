package com.hirematch.hirematch_api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {

    @NotBlank(message = "El tipo de perfil es obligatorio")
    private String tipoPerfil;

    
    @Size(max = 150, message = "El nombre de la empresa no puede exceder 150 caracteres")
    private String nombreEmpresa; // New field for empresa profiles

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @Size(max = 255, message = "La ubicación no puede exceder 255 caracteres")
    private String ubicacion;

    @Size(max = 500, message = "Las habilidades no pueden exceder 500 caracteres")
    private String habilidades;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Size(max = 255, message = "El sitio web no puede exceder 255 caracteres")
    private String sitioWeb;

    @Size(max = 2000, message = "La experiencia no puede exceder 2000 caracteres")
    private String experiencia;

    @Size(max = 2000, message = "La educación no puede exceder 2000 caracteres")
    private String educacion;

    @Size(max = 2000, message = "Las certificaciones no pueden exceder 2000 caracteres")
    private String certificaciones;

    @Size(max = 500, message = "Los intereses no pueden exceder 500 caracteres")
    private String intereses;
}