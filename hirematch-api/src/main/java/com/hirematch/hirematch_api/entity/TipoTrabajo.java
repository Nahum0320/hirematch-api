package com.hirematch.hirematch_api.entity;

public enum TipoTrabajo {
    REMOTO("Remoto"),
    PRESENCIAL("Presencial"),
    HIBRIDO("Híbrido");

    private final String descripcion;

    TipoTrabajo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
