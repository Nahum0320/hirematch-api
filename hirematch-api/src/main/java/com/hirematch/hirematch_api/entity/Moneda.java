package com.hirematch.hirematch_api.entity;

public enum Moneda {
    USD("$", "Dólares"),
    CRC("₡", "Colones"),
    EUR("€", "Euros"),
    MXN("$", "Pesos Mexicanos"),
    CAD("C$", "Dólares Canadienses");

    private final String simbolo;
    private final String nombre;

    Moneda(String simbolo, String nombre) {
        this.simbolo = simbolo;
        this.nombre = nombre;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getNombre() {
        return nombre;
    }
}