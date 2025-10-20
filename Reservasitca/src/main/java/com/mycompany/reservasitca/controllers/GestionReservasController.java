/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservasitca.controllers;
import com.mycompany.reservasitca.models.*;
import exceptions.ConflictoHorarioException;
import exceptions.ReglaReservaException;
import utilities.Persistencia;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author alex
 */

public class GestionReservasController {
    private List<Aula> aulas;
    private List<Reserva> reservas;
    private AtomicInteger aulaIdCounter = new AtomicInteger(1);
    private AtomicInteger reservaIdCounter = new AtomicInteger(1);
    
    public GestionReservasController() {
        // Cargar datos al iniciar
        this.aulas = Persistencia.cargarAulas();
        
        // Inicializar el contador de IDs de Aulas
        aulas.stream()
             .map(a -> Integer.parseInt(a.getId()))
             .max(Comparator.naturalOrder())
             .ifPresent(maxId -> aulaIdCounter.set(maxId + 1));
             
        // Usar un mapa para buscar aulas fácilmente al cargar reservas
        Map<String, Aula> mapaAulas = aulas.stream()
                                           .collect(Collectors.toMap(Aula::getId, a -> a));
                                           
        this.reservas = Persistencia.cargarReservas(mapaAulas);
        
        // Inicializar el contador de IDs de Reservas
        reservas.stream()
                .map(r -> Integer.parseInt(r.getId()))
                .max(Comparator.naturalOrder())
                .ifPresent(maxId -> reservaIdCounter.set(maxId + 1));
        
        // Actualizar estados a HISTORICA al cargar
        actualizarEstadosHistoricos();
    }
    
    // --- GESTIÓN DE AULAS ---

    public void registrarAula(String nombre, TipoAula tipo, int capacidad) {
        String id = String.valueOf(aulaIdCounter.getAndIncrement());
        Aula nuevaAula = new Aula(id, nombre, tipo, capacidad);
        aulas.add(nuevaAula);
        Persistencia.guardarAulas(aulas);
    }
    
    public List<Aula> listarAulas(boolean ordenar) {
        if (ordenar) {
            return aulas.stream()
                        .sorted(Comparator.comparing(Aula::getNombre))
                        .collect(Collectors.toList());
        }
        return aulas;
    }
    
    public Aula buscarAulaPorId(String id) {
        return aulas.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);
    }

    public boolean modificarAula(String id, String nombre, int capacidad) {
        Aula aula = buscarAulaPorId(id);
        if (aula != null) {
            aula.setNombre(nombre);
            aula.setCapacidad(capacidad);
            Persistencia.guardarAulas(aulas);
            return true;
        }
        return false;
    }

    // --- GESTIÓN DE RESERVAS ---
    
    // Método para verificar conflictos de horario (uso de Streams y lógica de tiempo)
    private void verificarConflicto(Aula aula, LocalDate fecha, LocalTime hInicio, LocalTime hFin) throws ConflictoHorarioException {
        boolean conflicto = reservas.stream()
            .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
            .filter(r -> r.getAula().getId().equals(aula.getId()))
            .filter(r -> r.getFecha().isEqual(fecha))
            .anyMatch(r -> {
                // Verificar solapamiento: (NuevoInicio < FinExistente) AND (NuevoFin > InicioExistente)
                return (hInicio.isBefore(r.getHoraFin()) && hFin.isAfter(r.getHoraInicio()));
            });
            
        if (conflicto) {
            throw new ConflictoHorarioException(
                "Conflicto de horario: El aula " + aula.getNombre() + 
                " ya está reservada en el rango " + hInicio + " a " + hFin + " en esa fecha."
            );
        }
    }
    
    // Método polimórfico para el registro
    public void registrarReserva(Reserva reserva) throws ReglaReservaException, ConflictoHorarioException {
        // 1. Validar reglas específicas (Polimorfismo con Validable)
        ((Validable) reserva).validar(); 
        
        // 2. Verificar conflicto de horario
        verificarConflicto(reserva.getAula(), reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin());
        
        // 3. Registrar
        String id = String.valueOf(reservaIdCounter.getAndIncrement());
        reserva.setId(id);
        reservas.add(reserva);
        Persistencia.guardarReservas(reservas);
    }
    
    // Búsqueda por responsable uso de Streams
    public List<Reserva> buscarReservasPorResponsable(String texto) {
        String textoLower = texto.toLowerCase();
        return reservas.stream()
                       .filter(r -> r.getResponsable().toLowerCase().contains(textoLower))
                       .collect(Collectors.toList());
    }
    
    public Reserva buscarReservaPorId(String id) {
        return reservas.stream()
                       .filter(r -> r.getId().equals(id))
                       .findFirst()
                       .orElse(null);
    }
    
    public boolean modificarReserva(String id, LocalTime nuevaHInicio, LocalTime nuevaHFin, String nuevoResponsable) {
        Reserva r = buscarReservaPorId(id);
        if (r != null && r.getEstado() == EstadoReserva.ACTIVA) {
            try {
                LocalTime originalHInicio = r.getHoraInicio();
                LocalTime originalHFin = r.getHoraFin();
                
                r.setHoraInicio(nuevaHInicio);
                r.setHoraFin(nuevaHFin);
                
                r.setResponsable(nuevoResponsable);
                Persistencia.guardarReservas(reservas);
                return true;
            } catch (Exception e) {
                System.err.println("Error al modificar la reserva: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    public boolean cancelarReserva(String id) {
        Reserva r = buscarReservaPorId(id);
        if (r != null && r.getEstado() == EstadoReserva.ACTIVA) {
            r.setEstado(EstadoReserva.CANCELADA);
            Persistencia.guardarReservas(reservas);
            return true;
        }
        return false;
    }
    
    // Método para pasar las reservas antiguas a HISTORICA
    public void actualizarEstadosHistoricos() {
        LocalDate hoy = LocalDate.now();
        reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                .filter(r -> r.getFecha().isBefore(hoy))
                .forEach(r -> r.setEstado(EstadoReserva.HISTORICA));
        Persistencia.guardarReservas(reservas);
    }

    // --- REPORTES (Uso intensivo de Streams de Java 8) ---
    
    public String generarReporteTopAulas() {
        
        // Calcular la duración en horas (uso de Duration) y agrupar por nombre de aula
        Map<String, Long> horasPorAula = reservas.stream()
            .filter(r -> r.getEstado() != EstadoReserva.CANCELADA) // No contar canceladas
            .collect(Collectors.groupingBy(
                r -> r.getAula().getNombre(),
                Collectors.summingLong(r -> Duration.between(r.getHoraInicio(), r.getHoraFin()).toHours())
            ));

        // Ordenar y limitar al top 3
        String reporte = horasPorAula.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(3)
            .map(entry -> String.format("   %s: %d horas", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("\n"));
            
        return "--- TOP 3 AULAS CON MÁS HORAS RESERVADAS ---\n" + (reporte.isEmpty() ? "No hay datos de reservas." : reporte);
    }
    
    public String generarReporteOcupacionPorTipoAula() {
        // Agrupar por TipoAula (del aula de la reserva) y contar
        Map<TipoAula, Long> conteoPorTipoAula = reservas.stream()
            .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
            .collect(Collectors.groupingBy(
                r -> r.getAula().getTipo(),
                Collectors.counting()
            ));

        String reporte = conteoPorTipoAula.entrySet().stream()
            .map(entry -> String.format("   %s: %d reservas activas", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("\n"));
            
        return "--- OCUPACIÓN POR TIPO DE AULA ---\n" + (reporte.isEmpty() ? "No hay reservas activas." : reporte);
    }
    
    public String generarReporteDistribucionPorTipoReserva() {
        // Agrupar por el tipo de reserva (Clase, Práctica, Evento) usando el método abstracto (Polimorfismo)
        Map<String, Long> distribucion = reservas.stream()
            .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
            .collect(Collectors.groupingBy(
                Reserva::getTipoReserva, // Uso de referencia a método
                Collectors.counting()
            ));

        String reporte = distribucion.entrySet().stream()
            .map(entry -> String.format("   %s: %d reservas", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("\n"));
            
        return "--- DISTRIBUCIÓN POR TIPO DE RESERVA ---\n" + (reporte.isEmpty() ? "No hay datos de reservas." : reporte);
    }
    
    public void guardarDatos() {
        Persistencia.guardarAulas(aulas);
        Persistencia.guardarReservas(reservas);
    }
}
