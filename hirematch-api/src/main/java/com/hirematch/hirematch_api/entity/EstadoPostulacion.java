package com.hirematch.hirematch_api.entity;

public enum EstadoPostulacion {
    PENDING("Pendiente"),
    SUPERLIKE("Super Like"),
    MATCHED("Match"),
    REJECTED("Rechazado"),
    ACCEPTED("Aceptado");

    private final String descripcion;

    EstadoPostulacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}