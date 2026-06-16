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
        System.out.print("Seleccione su servicio: ");
        System.out.print("\n 1. General");
        System.out.print("\n 2. Otro\n");
        int puerto = Integer.parseInt(scanner.nextLine().trim());

        Socket socket = null;

        if(puerto == 1){
            puerto = 35001;
        } else {
            System.out.println("su respuesta no es valida");
        }
        try {
            socket = new Socket("127.0.0.1", puerto);
            System.out.println("si necesita un turno presione cualquier tecla (\"bye\" para salir)");
        } catch (IOException e) {
            System.err.println("No se pudo conectar al puerto " + puerto + " Está corriendo el servidor?");
            System.exit(1);
        }

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            System.out.println("Respuesta: " + in.readLine());
            if (userInput.equalsIgnoreCase("bye")) break;
        }

        out.close();
        in.close();
        socket.close();
    }
}
