package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ProfileResponse {
    private Long perfilId;
    private String tipoPerfil;
    private String nombreEmpresa;
    private Long empresaId; // Add this field
    private String descripcion;
    private String ubicacion;
    private String habilidades;
    private String telefono;
    private String sitioWeb;
    private String experiencia;
    private String educacion;
    private String certificaciones;
    private String intereses;
    private String fotoUrl;
    private String mensaje;
}