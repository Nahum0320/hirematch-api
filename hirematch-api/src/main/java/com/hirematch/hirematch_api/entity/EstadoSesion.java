package com.hirematch.hirematch_api.entity;

public enum EstadoSesion {
    ACTIVA("ACTIVA"),
    EXPIRADA("EXPIRADA"),
    CANCELADA("CANCELADA");
    
    private final String descripcion;
    
    EstadoSesion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}