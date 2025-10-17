package com.hirematch.hirematch_api.entity;

public enum EstadoReporte {
    PENDIENTE("Reporte recibido y pendiente revisión"),
    EN_REVISION("En proceso de revisión por administradores"),
    RESUELTO("Reporte procesado y resuelto"),
    RECHAZADO("Reporte rechazado por falta de evidencia o invalidez");

    private final String descripcion;

    EstadoReporte(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}