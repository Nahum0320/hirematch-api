package com.hirematch.hirematch_api.DTO;

public class UsuarioSummary {
    public Long usuarioId;
    public String nombre;
    public String apellido;
    public String email;

    public UsuarioSummary(Long usuarioId, String nombre, String apellido, String email) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
    }
}
