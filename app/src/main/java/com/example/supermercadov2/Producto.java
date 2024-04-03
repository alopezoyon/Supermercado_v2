package com.example.supermercadov2;

//Clase que implementa los productos que se guardan en la base de datos.
//Cada producto tiene un nombre y un precio asociado.
public class Producto {
    private String nombre;
    private double precio;

    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}
