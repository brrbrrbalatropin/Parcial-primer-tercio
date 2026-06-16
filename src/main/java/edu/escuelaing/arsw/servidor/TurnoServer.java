package edu.escuelaing.arsw.servidor;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TurnoServer {

    static ConcurrentHashMap<Integer, String> lista = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, PrintWriter> clientesConectados = new ConcurrentHashMap<>();
    static int turnoActual = 1;

    public static void main(String[] args) throws IOException {
        Thread pacientes = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(35001)) {
                System.out.println("Servidor PACIENTES en puerto 35001...");
                while (true) {
                    Socket cliente = ss.accept();
                    new Thread(() -> atenderPaciente(cliente)).start();
                }
            } catch (IOException e) {
                System.err.println("Error en servidor pacientes: " + e.getMessage());
            }
        });
        Thread recepcion = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(35002)) {
                System.out.println("Servidor RECEPCIÓN en puerto 35002...");
                while (true) {
                    Socket cliente = ss.accept();
                    new Thread(() -> atenderRecepcion(cliente)).start();
                }
            } catch (IOException e) {
                System.err.println("Error en servidor recepción: " + e.getMessage());
            }
        });

        pacientes.start();
        recepcion.start();
    }

    static void atenderPaciente(Socket socket) {
        int turnoAsignado = -1;
        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String input;
            while ((input = in.readLine()) != null) {
                if (input.equalsIgnoreCase("bye")) {
                    out.println("Hasta luego.");
                    break;
                }
                if (turnoAsignado == -1) {
                    synchronized (TurnoServer.class) {
                        turnoAsignado = turnoActual++;
                        lista.put(turnoAsignado, "CREATED");
                        clientesConectados.put(turnoAsignado, out);
                    }
                    System.out.println("Paciente Turno asignado: " + turnoAsignado);
                    out.println("Su turno asignado es: " + turnoAsignado + " - Estado: CREATED");
                    out.println("Espere aquí, recibirá un aviso cuando lo llamen");
                } else {
                    out.println("Ya tiene el turno " + turnoAsignado + " Estado actual: " + lista.get(turnoAsignado));
                }
            }
        } catch (IOException e) {
            System.err.println("Error con paciente: " + e.getMessage());
        } finally {
            if (turnoAsignado != -1) {
                clientesConectados.remove(turnoAsignado);
                System.out.println("Servidor Paciente turno " + turnoAsignado + " desconectado.");
            }
        }
    }

    static void atenderRecepcion(Socket socket) {
        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(" PANEL DE RECEPCIÓN");
            out.println("Comandos: LLAMAR <numero>, LISTAR, bye");

            String input;
            while ((input = in.readLine()) != null) {
                if (input.equalsIgnoreCase("bye")) {
                    out.println("Sesión de recepción cerrada.");
                    break;
                } else if (input.equalsIgnoreCase("listar")) {
                    if (lista.isEmpty()) {
                        out.println("No hay turnos registrados.");
                    } else {
                        lista.forEach((t, estado) ->
                                out.println("  Turno " + t + ": " + estado));
                    }
                    out.println("");
                } else if (input.toUpperCase().startsWith("LLAMAR ")) {
                    try {
                        int nroTurno = Integer.parseInt(input.substring(7).trim());
                        if (!lista.containsKey(nroTurno)) {
                            out.println("El turno " + nroTurno + " no existe.");
                            out.println("");
                            continue;
                        }

                        lista.put(nroTurno, "CALLED");
                        System.out.println("Recepción Turno " + nroTurno + " -> CALLED");
                        out.println("Turno " + nroTurno + " actualizado a: CALLED");
                        out.println("");

                        PrintWriter pacienteOut = clientesConectados.get(nroTurno);
                        if (pacienteOut != null) {
                            pacienteOut.println("Su turno " + nroTurno + " ha sido llamado");
                        } else {
                            out.println("(El paciente del turno " + nroTurno + " ya no está conectado)");
                            out.println("");
                        }
                    } catch (NumberFormatException e) {
                        out.println("Formato inválido. Usá: LLAMAR <número>");
                    }
                } else {
                    out.println("Comando no reconocido. Usá: LLAMAR <numero>, LISTAR, bye");
                }
            }
        } catch (IOException e) {
            System.err.println("Error con cliente recepción: " + e.getMessage());
        }
    }
}