package com.example.supermercadov2;

import java.util.List;

//Esta clase implementa los supermercados. Tenemos para cada uno su nombre, localización y la lista de productos que tiene.
public class Supermercado {
    private String nombre;
    private String localizacion;

    public Supermercado(String nombre, String localizacion) {
        this.nombre = nombre;
        this.localizacion = localizacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLocalizacion() {
        return localizacion;
    }
}

