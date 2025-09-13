package com.hirematch.hirematch_api.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilPublicoResponse {
    private Long perfilId;
    private String nombreCompleto; // nombre + apellido del usuario
    private String email;
    private String tipoPerfil;
    private String descripcion;
    private String ubicacion;
    private String habilidades;
    private String experiencia;
    private String educacion;
    private String certificaciones;
    private String intereses;
    private String fotoUrl; // base64 de la foto si existe
}
