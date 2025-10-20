/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.reservasitca;
import com.mycompany.reservasitca.controllers.GestionReservasController;
import com.mycompany.reservasitca.models.*;
import utilities.Persistencia;
import exceptions.ConflictoHorarioException;
import exceptions.ReglaReservaException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
/**
 *
 * @author alex
 */

public class Reservasitca {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final GestionReservasController CONTROLLER = new GestionReservasController();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) {
        System.out.println("  SISTEMA DE GESTION DE RESERVAS DE AULAS ");
        
        int opcion;
        do {
            mostrarMenuPrincipal();
            opcion = leerEntero("Seleccione una opcion: ");
            
            try {
                switch (opcion) {
                    case 1: gestionarAulas(); break;
                    case 2: gestionarReservas(); break;
                    case 3: generarReportes(); break;
                    case 4: 
                        CONTROLLER.guardarDatos();
                        System.out.println("Saliendo del sistema. Hasta pronto!");
                        break;
                    default:
                        System.out.println("Opcion no valida. Intente de nuevo.");
                }
            } catch (Exception e) {
                System.err.println("\nERROR INESPERADO: " + e.getMessage() + "\n");
            }
        } while (opcion != 4);
    }

    // --- METODOS DEL MENU Y NAVEGACION ---
    private static void mostrarMenuPrincipal() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Gestion de Aulas");
        System.out.println("2. Gestion de Reservas");
        System.out.println("3. Generacion de Reportes");
        System.out.println("4. Salir y Guardar");
    }

    private static void gestionarAulas() {
        int opcion;
        do {
            System.out.println("\n--- GESTION DE AULAS ---");
            System.out.println("1. Registrar Aula");
            System.out.println("2. Listar Aulas");
            System.out.println("3. Modificar Aula");
            System.out.println("4. Volver al Menu Principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: registrarAula(); break;
                case 2: listarAulas(); break;
                case 3: modificarAula(); break;
                case 4: break;
                default: System.out.println("Opcion no valida.");
            }
        } while (opcion != 4);
    }
    
    // --- LOGICA DE AULAS ---
    private static void registrarAula() {
        System.out.println("\n--- REGISTRAR AULA ---");
        String nombre = leerCadena("Nombre del aula: ");
        int capacidad = leerEntero("Capacidad: ");
        
        System.out.println("Tipos disponibles: TEORICA, LABORATORIO, AUDITORIO");
        String tipoStr = leerCadena("Tipo de aula: ").toUpperCase();
        try {
            TipoAula tipo = TipoAula.valueOf(tipoStr);
            CONTROLLER.registrarAula(nombre, tipo, capacidad);
            System.out.println("Aula registrada con exito.");
        } catch (IllegalArgumentException e) {
            System.out.println("Tipo de aula no valido. Intente de nuevo.");
        }
    }
    
    private static void listarAulas() {
        System.out.println("\n--- LISTADO DE AULAS ---");
        String ordenarStr = leerCadena("Desea ordenar por nombre? (s/n): ").toLowerCase();
        boolean ordenar = "s".equals(ordenarStr);
        
        List<Aula> aulas = CONTROLLER.listarAulas(ordenar);
        if (aulas.isEmpty()) {
            System.out.println("No hay aulas registradas.");
            return;
        }
        aulas.forEach(System.out::println);
    }
    
    private static void modificarAula() {
        String id = leerCadena("ID del aula a modificar: ");
        Aula aula = CONTROLLER.buscarAulaPorId(id);
        
        if (aula == null) {
            System.out.println("Aula no encontrada.");
            return;
        }
        
        System.out.println("Aula actual: " + aula);
        String nuevoNombre = leerCadena("Nuevo nombre (dejar vacio para no cambiar): ");
        String nuevaCapacidadStr = leerCadena("Nueva capacidad (dejar vacio para no cambiar): ");
        
        String nombre = nuevoNombre.isEmpty() ? aula.getNombre() : nuevoNombre;
        int capacidad = aula.getCapacidad();
        
        if (!nuevaCapacidadStr.isEmpty()) {
            try {
                capacidad = Integer.parseInt(nuevaCapacidadStr);
            } catch (NumberFormatException e) {
                System.out.println("Capacidad no valida. Se mantendra la anterior.");
            }
        }
        
        if (CONTROLLER.modificarAula(id, nombre, capacidad)) {
            System.out.println("Aula modificada con exito.");
        } else {
            System.out.println("No se pudo modificar el aula.");
        }
    }

    // --- LOGICA DE RESERVAS ---
    private static void gestionarReservas() {
        int opcion;
        do {
            System.out.println("\n--- GESTION DE RESERVAS ---");
            System.out.println("1. Registrar Reserva");
            System.out.println("2. Buscar Reserva por Responsable");
            System.out.println("3. Modificar Reserva (Horario/Responsable)");
            System.out.println("4. Cancelar Reserva");
            System.out.println("5. Volver al Menu Principal");
            opcion = leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1: solicitarRegistroReserva(); break;
                case 2: buscarReserva(); break;
                case 3: modificarReserva(); break;
                case 4: cancelarReserva(); break;
                case 5: break;
                default: System.out.println("Opcion no valida.");
            }
        } while (opcion != 5);
    }

    private static void solicitarRegistroReserva() {
        System.out.println("\n--- REGISTRAR RESERVA ---");
        
        String aulaId = leerCadena("ID del aula a reservar: ");
        Aula aula = CONTROLLER.buscarAulaPorId(aulaId);
        if (aula == null) {
            System.out.println("Aula con ID " + aulaId + " no encontrada.");
            return;
        }
        System.out.println("Aula seleccionada: " + aula);
        
        LocalDate fecha;
        LocalTime hInicio, hFin;
        try {
            fecha = leerFecha("Fecha de la reserva (dd/MM/yyyy): ");
            hInicio = leerHora("Hora de inicio (HH:mm): ");
            hFin = leerHora("Hora de fin (HH:mm): ");
            
            if (!hFin.isAfter(hInicio)) {
                 System.out.println("La hora de fin debe ser posterior a la hora de inicio.");
                 return;
            }
        } catch (DateTimeParseException e) {
            System.out.println("Formato de fecha u hora incorrecto.");
            return;
        }

        String responsable = leerCadena("Nombre del responsable: ");
        
        System.out.println("Tipos de Reserva: 1. Clase, 2. Practica, 3. Evento");
        int tipoReservaOp = leerEntero("Seleccione el tipo de reserva: ");
        
        try {
            Reserva nuevaReserva = null;
            String tempId = "0"; // ID temporal
            
            switch (tipoReservaOp) {
                case 1: 
                    nuevaReserva = new ReservaClase(tempId, aula, fecha, hInicio, hFin, responsable);
                    break;
                case 2: 
                    nuevaReserva = new ReservaPractica(tempId, aula, fecha, hInicio, hFin, responsable);
                    break;
                case 3: 
                    System.out.println("Tipos de Evento: CONFERENCIA, TALLER, REUNION");
                    String tipoEventoStr = leerCadena("Tipo de evento: ").toUpperCase();
                    TipoEvento tipoEvento = TipoEvento.valueOf(tipoEventoStr);
                    nuevaReserva = new ReservaEvento(tempId, aula, fecha, hInicio, hFin, responsable, tipoEvento);
                    break;
                default:
                    System.out.println("Tipo de reserva no valido.");
                    return;
            }
            
            if (nuevaReserva != null) {
                CONTROLLER.registrarReserva(nuevaReserva);
                System.out.println("Reserva registrada con exito. ID asignado: " + nuevaReserva.getId());
            }
            
        } catch (ReglaReservaException e) {
            System.out.println("ERROR DE REGLA: " + e.getMessage());
        } catch (ConflictoHorarioException e) {
            System.out.println("ERROR DE HORARIO: " + e.getMessage());
        } catch (IllegalArgumentException e) {
             System.out.println("Tipo de evento no valido.");
        }
    }
    
    private static void buscarReserva() {
        System.out.println("\n--- BUSCAR RESERVA ---");
        String texto = leerCadena("Escriba parte del nombre del responsable: ");
        
        List<Reserva> resultados = CONTROLLER.buscarReservasPorResponsable(texto);
        
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron reservas para ese responsable.");
            return;
        }
        
        System.out.println("--- Resultados de Busqueda ---");
        resultados.forEach(System.out::println);
    }
    
    private static void modificarReserva() {
        System.out.println("\n--- MODIFICAR RESERVA ---");
        String id = leerCadena("ID de la reserva a modificar: ");
        Reserva r = CONTROLLER.buscarReservaPorId(id);
        
        if (r == null) {
            System.out.println("Reserva no encontrada.");
            return;
        }
        
        if (r.getEstado() != EstadoReserva.ACTIVA) {
            System.out.println("Solo se pueden modificar reservas ACTIVAS. Estado actual: " + r.getEstado());
            return;
        }
        
        System.out.println("Reserva actual: " + r);
        
        try {
            LocalTime nuevaHInicio = leerHoraOpcional("Nueva hora de inicio (HH:mm, dejar vacio para no cambiar: ", r.getHoraInicio());
            LocalTime nuevaHFin = leerHoraOpcional("Nueva hora de fin (HH:mm, dejar vacio para no cambiar: ", r.getHoraFin());
            String nuevoResponsable = leerCadenaOpcional("Nuevo responsable (dejar vacio para no cambiar): ", r.getResponsable());
            
            if (CONTROLLER.modificarReserva(id, nuevaHInicio, nuevaHFin, nuevoResponsable)) {
                System.out.println("Reserva modificada con exito.");
            } else {
                System.out.println("No se pudo modificar la reserva (posible conflicto de horario).");
            }
            
        } catch (DateTimeParseException e) {
            System.out.println("Formato de hora incorrecto.");
        }
    }
    
    private static void cancelarReserva() {
        System.out.println("\n--- CANCELAR RESERVA ---");
        String id = leerCadena("ID de la reserva a cancelar: ");
        
        if (CONTROLLER.cancelarReserva(id)) {
            System.out.println("Reserva " + id + " CANCELADA con exito.");
        } else {
            System.out.println("Reserva no encontrada o ya esta cancelada/historica.");
        }
    }
    
    // --- LOGICA DE REPORTES ---
    private static void generarReportes() {
        int opcion;
        do {
            System.out.println("\n--- REPORTES ---");
            System.out.println("1. Top 3 Aulas con mas Horas Reservadas");
            System.out.println("2. Ocupacion por Tipo de Aula");
            System.out.println("3. Distribucion por Tipo de Reserva");
            System.out.println("4. Volver al Menu Principal");
            opcion = leerEntero("Seleccione una opcion: ");
            
            String reporte = "";
            String nombreArchivo = "";

            switch (opcion) {
                case 1: 
                    reporte = CONTROLLER.generarReporteTopAulas();
                    nombreArchivo = "reporte_top_aulas.txt";
                    break;
                case 2: 
                    reporte = CONTROLLER.generarReporteOcupacionPorTipoAula();
                    nombreArchivo = "reporte_ocupacion_tipo.txt";
                    break;
                case 3: 
                    reporte = CONTROLLER.generarReporteDistribucionPorTipoReserva();
                    nombreArchivo = "reporte_distribucion_reserva.txt";
                    break;
                case 4: return;
                default: System.out.println("Opcion no valida."); continue;
            }
            
            System.out.println("\n" + reporte);
            
            String exportar = leerCadena("Desea exportar este reporte a '" + nombreArchivo + "'? (s/n): ").toLowerCase();
            if ("s".equals(exportar)) {
                Persistencia.exportarReporte(nombreArchivo, reporte);
            }
            
        } while (opcion != 4);
    }

    // --- UTILITIES DE ENTRADA CON VALIDACION ---
    private static String leerCadena(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = SCANNER.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("La entrada no puede estar vacia.");
            }
        } while (input.isEmpty());
        return input;
    }
    
    private static String leerCadenaOpcional(String prompt, String valorActual) {
        System.out.print(prompt);
        String input = SCANNER.nextLine().trim();
        return input.isEmpty() ? valorActual : input;
    }

    private static int leerEntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un numero entero valido.");
            }
        }
    }

    private static LocalDate leerFecha(String prompt) throws DateTimeParseException {
        String input = leerCadena(prompt);
        return LocalDate.parse(input, DATE_FORMAT);
    }
    
    private static LocalTime leerHora(String prompt) throws DateTimeParseException {
        String input = leerCadena(prompt);
        return LocalTime.parse(input, TIME_FORMAT);
    }
    
    private static LocalTime leerHoraOpcional(String prompt, LocalTime valorActual) throws DateTimeParseException {
        System.out.print(prompt);
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return valorActual;
        }
        return LocalTime.parse(input, TIME_FORMAT);
    }
}