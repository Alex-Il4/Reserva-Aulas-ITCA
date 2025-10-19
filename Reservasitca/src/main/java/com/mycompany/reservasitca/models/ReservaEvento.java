/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservasitca.models;
import exceptions.ReglaReservaException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author alex
 */

public class ReservaEvento extends Reserva implements Validable {
    private TipoEvento tipoEvento;

    public ReservaEvento(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable, TipoEvento tipoEvento) {
        super(id, aula, fecha, horaInicio, horaFin, responsable);
        this.tipoEvento = tipoEvento;
    }
    
    public ReservaEvento(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable, EstadoReserva estado, TipoEvento tipoEvento) {
        super(id, aula, fecha, horaInicio, horaFin, responsable, estado);
        this.tipoEvento = tipoEvento;
    }

    public TipoEvento getTipoEvento() { return tipoEvento; }

    @Override
    public void validar() throws ReglaReservaException {
        // Regla específica: solo en aulas AUDITORIO
        if (this.getAula().getTipo() != TipoAula.AUDITORIO) {
            throw new ReglaReservaException("Una Reserva de Evento solo puede realizarse en aulas AUDITORIO.");
        }
    }

    @Override
    public String getTipoReserva() {
        return "Evento";
    }
    
    @Override
    public String toString() {
        return String.format("Reserva Evento - ID: %s, Aula: %s (%s), Fecha: %s, Horario: %s a %s, Responsable: %s, Tipo: %s, Estado: %s",
                id, aula.getNombre(), aula.getTipo(), fecha, horaInicio, horaFin, responsable, tipoEvento, estado);
    }
    
    public String toCsv() {
        return getTipoReserva() + "|" + toCsvBase() + "|" + tipoEvento.name();
    }
}
