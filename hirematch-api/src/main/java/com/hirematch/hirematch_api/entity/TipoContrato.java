package com.hirematch.hirematch_api.entity;

public enum TipoContrato {
    TIEMPO_COMPLETO("Tiempo Completo"),
    MEDIO_TIEMPO("Medio Tiempo"),
    CONTRATO("Por Contrato"),
    TEMPORAL("Temporal"),
    FREELANCE("Freelance"),
    PRACTICAS("Prácticas");

    private final String descripcion;

    TipoContrato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}