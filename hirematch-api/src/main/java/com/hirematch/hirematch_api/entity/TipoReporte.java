package com.hirematch.hirematch_api.entity;

public enum TipoReporte {
    SPAM("Envío de contenido no solicitado o repetitivo"),
    ACOSO("Comportamiento ofensivo, amenazas o hostigamiento"),
    CONTENIDO_INAPROPIADO("Contenido ofensivo, discriminatorio o ilegal"),
    FRAUDE("Información falsa, estafas o perfiles falsos"),
    OFERTA_ENGAÑOSA("Oferta laboral con información falsa o engañosa"),
    OTRO("Otro motivo no especificado");

    private final String descripcion;

    TipoReporte(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}