/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities;
import com.mycompany.reservasitca.models.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author alex
 */

public class Persistencia {
    private static final String FILE_AULAS = "aulas.txt";
    private static final String FILE_RESERVAS = "reservas.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_TIME;

    // Métodos para Aulas
    public static void guardarAulas(List<Aula> aulas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_AULAS))) {
            aulas.forEach(aula -> pw.println(aula.toCsv()));
            System.out.println("Aulas guardadas correctamente.");
        } catch (IOException e) {
            System.err.println("Error al guardar aulas: " + e.getMessage());
        }
    }

    public static List<Aula> cargarAulas() {
        List<Aula> aulas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_AULAS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    aulas.add(Aula.fromCsv(linea));
                }
            }
            System.out.println("Aulas cargadas (" + aulas.size() + ").");
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de aulas no encontrado. Se creará uno nuevo al guardar.");
        } catch (IOException e) {
            System.err.println("Error de lectura al cargar aulas: " + e.getMessage());
        }
        return aulas;
    }
    
    // Métodos para Reservas
    public static void guardarReservas(List<Reserva> reservas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_RESERVAS))) {
            reservas.forEach(reserva -> {
                if (reserva instanceof ReservaClase) {
                    pw.println(((ReservaClase) reserva).toCsv());
                } else if (reserva instanceof ReservaPractica) {
                    pw.println(((ReservaPractica) reserva).toCsv());
                } else if (reserva instanceof ReservaEvento) {
                    pw.println(((ReservaEvento) reserva).toCsv());
                }
            });
            System.out.println("Reservas guardadas correctamente.");
        } catch (IOException e) {
            System.err.println("Error al guardar reservas: " + e.getMessage());
        }
    }

    public static List<Reserva> cargarReservas(Map<String, Aula> mapaAulas) {
        List<Reserva> reservas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_RESERVAS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    reservas.add(parseReserva(linea, mapaAulas));
                }
            }
            System.out.println("Reservas cargadas (" + reservas.size() + ").");
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de reservas no encontrado. Se creara uno nuevo al guardar.");
        } catch (Exception e) {
            System.err.println("Error al cargar reservas: " + e.getMessage());
            e.printStackTrace();
        }
        return reservas;
    }
    
    // Método auxiliar polimórfico para cargar
    private static Reserva parseReserva(String linea, Map<String, Aula> mapaAulas) {
        String[] partes = linea.split("\\|");
        String tipo = partes[0]; // Tipo de reserva
        String id = partes[1];
        Aula aula = mapaAulas.get(partes[2]); // Aula ya cargada
        LocalDate fecha = LocalDate.parse(partes[3], DATE_FORMAT);
        LocalTime hInicio = LocalTime.parse(partes[4], TIME_FORMAT);
        LocalTime hFin = LocalTime.parse(partes[5], TIME_FORMAT);
        String responsable = partes[6];
        EstadoReserva estado = EstadoReserva.valueOf(partes[7]);

        if (aula == null) {
             throw new IllegalArgumentException("Aula con ID " + partes[2] + " no encontrada para la reserva " + id);
        }

        switch (tipo) {
            case "Clase":
                return new ReservaClase(id, aula, fecha, hInicio, hFin, responsable, estado);
            case "Práctica":
                return new ReservaPractica(id, aula, fecha, hInicio, hFin, responsable, estado);
            case "Evento":
                TipoEvento tipoEvento = TipoEvento.valueOf(partes[8]); // El evento tiene un campo extra
                return new ReservaEvento(id, aula, fecha, hInicio, hFin, responsable, estado, tipoEvento);
            default:
                throw new IllegalArgumentException("Tipo de reserva desconocido: " + tipo);
        }
    }
    
    // Método para exportar reportes a TXT
    public static void exportarReporte(String nombreArchivo, String contenido) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo))) {
            pw.println(contenido);
            System.out.println("Reporte exportado a: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al exportar reporte: " + e.getMessage());
        }
    }
}
