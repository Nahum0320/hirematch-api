package com.hirematch.hirematch_api.entity;

public enum EstadoOferta {
    BORRADOR("Borrador"),
    ACTIVA("Activa"),
    PAUSADA("Pausada"),
    CERRADA("Cerrada"),
    EXPIRADA("Expirada");

    private final String descripcion;

    EstadoOferta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}