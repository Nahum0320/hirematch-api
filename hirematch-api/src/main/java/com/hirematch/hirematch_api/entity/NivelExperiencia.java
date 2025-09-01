package com.hirematch.hirematch_api.entity;

public enum NivelExperiencia {
    ESTUDIANTE("Estudiante/Sin experiencia"),
    JUNIOR("Junior (0-2 años)"),
    SEMI_SENIOR("Semi Senior (2-4 años)"),
    SENIOR("Senior (4-7 años)"),
    LEAD("Lead/Team Lead (7+ años)"),
    DIRECTOR("Director/Manager (10+ años)");

    private final String descripcion;

    NivelExperiencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}