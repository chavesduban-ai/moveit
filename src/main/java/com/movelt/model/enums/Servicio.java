package com.movelt.model.enums;

public enum Servicio {
    express(12000), standard(8000), economy(5000);

    private final int precio;
    Servicio(int precio) { this.precio = precio; }
    public int getPrecio() { return precio; }
}
