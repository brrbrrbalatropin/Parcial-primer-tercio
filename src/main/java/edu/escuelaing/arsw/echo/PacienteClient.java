package edu.escuelaing.arsw.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
public class PacienteClient {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Seleccione su servicio:");
        System.out.print("\n 1. General");
        System.out.print("\n 2. Otro\n");
        int opcion = Integer.parseInt(scanner.nextLine().trim());

        int puerto;
        if (opcion == 1) {
            puerto = 35001;
        } else if (opcion == 2) {
            puerto = 35002;
        } else {
            System.out.println("su respuesta no es valida");
            return;
        }

        Socket socket;
        try {
            socket = new Socket("127.0.0.1", puerto);
            System.out.println("si necesita un turno presione cualquier tecla (\"bye\" para salir)");
        } catch (IOException e) {
            System.err.println("No se pudo conectar al puerto " + puerto + " Está corriendo el servidor?");
            return;
        }

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        Thread listener = new Thread(() -> {
            try {
                String serverMsg;
                while ((serverMsg = in.readLine()) != null) {
                    System.out.println("\nServidor: " + serverMsg);
                }
            } catch (IOException e) {
                System.out.println("\nConexión terminada");
            }
        });
        listener.setDaemon(true);
        listener.start();
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            if (userInput.equalsIgnoreCase("bye")) break;
        }

        out.close();
        in.close();
        socket.close();
    }
}