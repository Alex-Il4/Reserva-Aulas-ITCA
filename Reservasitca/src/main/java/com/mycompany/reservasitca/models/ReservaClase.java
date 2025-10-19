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

// Implementa herencia de Reserva
public class ReservaClase extends Reserva implements Validable {

    public ReservaClase(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable) {
        super(id, aula, fecha, horaInicio, horaFin, responsable);
    }
    
    public ReservaClase(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable, EstadoReserva estado) {
        super(id, aula, fecha, horaInicio, horaFin, responsable, estado);
    }

    @Override
    public void validar() throws ReglaReservaException {
        // Regla específica: solo en aulas TEORICA o LABORATORIO
        TipoAula tipo = this.getAula().getTipo();
        if (tipo != TipoAula.TEORICA && tipo != TipoAula.LABORATORIO) {
            throw new ReglaReservaException("Una Reserva de Clase solo puede realizarse en aulas TEORICA o LABORATORIO.");
        }
    }

    @Override
    public String getTipoReserva() {
        return "Clase";
    }
    
    @Override
    public String toString() {
        return String.format("Reserva Clase - ID: %s, Aula: %s (%s), Fecha: %s, Horario: %s a %s, Responsable: %s, Estado: %s",
                id, aula.getNombre(), aula.getTipo(), fecha, horaInicio, horaFin, responsable, estado);
    }
    
    public String toCsv() {
        return getTipoReserva() + "|" + toCsvBase();
    }
}
