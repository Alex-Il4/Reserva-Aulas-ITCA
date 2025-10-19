/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservasitca.models;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author alex
 */

// Clase abstracta para la herencia y polimorfismo
public abstract class Reserva {
    protected String id;
    protected Aula aula;
    protected LocalDate fecha;
    protected LocalTime horaInicio;
    protected LocalTime horaFin;
    protected String responsable;
    protected EstadoReserva estado;

    // Constructor base
    public Reserva(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable) {
        this.id = id;
        this.aula = aula;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.responsable = responsable;
        this.estado = EstadoReserva.ACTIVA; // Estado inicial
    }
    
    // Sobrecarga para cargar desde persistencia, incluye estado paara saber si es activo o no
    public Reserva(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable, EstadoReserva estado) {
        this(id, aula, fecha, horaInicio, horaFin, responsable);
        this.estado = estado;
    }

    // Getters y Setters
    public String getId() { return id; }
    public Aula getAula() { return aula; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public String getResponsable() { return responsable; }
    public EstadoReserva getEstado() { return estado; }

    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    
    // Método abstracto para el polimorfismo en reportes
    public abstract String getTipoReserva();
    
    // Método para la persistencia
    protected String toCsvBase() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ISO_DATE;
        DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;
        
        return id + "|" + aula.getId() + "|" + fecha.format(dateFormat) + "|" 
             + horaInicio.format(timeFormat) + "|" + horaFin.format(timeFormat) + "|"
             + responsable + "|" + estado.name();
    }
}
