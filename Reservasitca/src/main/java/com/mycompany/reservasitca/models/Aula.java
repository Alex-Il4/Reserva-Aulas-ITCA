/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservasitca.models;

/**
 *
 * @author alex
 */

// Uso de encapsulamiento
public class Aula {
    private String id;
    private String nombre;
    private TipoAula tipo;
    private int capacidad;

    public Aula(String id, String nombre, TipoAula tipo, int capacidad) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoAula getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    @Override
    public String toString() {
        return "ID: " + id + ", Nombre: " + nombre + ", Tipo: " + tipo + ", Capacidad: " + capacidad;
    }

    // Formato para persistencia
    public String toCsv() {
        return id + "|" + nombre + "|" + tipo.name() + "|" + capacidad;
    }

    // Método estático para cargar desde persistencia
    public static Aula fromCsv(String csv) {
        String[] partes = csv.split("\\|");
        return new Aula(partes[0], partes[1], TipoAula.valueOf(partes[2]), Integer.parseInt(partes[3]));
    }
}
