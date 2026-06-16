package edu.escuelaing.arsw.echo;

import java.io.*;
import java.net.*;

public class RecepcionClient {

    public static void main(String[] args) throws IOException {
        Socket socket;
        try {
            socket = new Socket("127.0.0.1", 35002);
            System.out.println("Conectado al panel de recepción.");
        } catch (IOException e) {
            System.err.println("No se pudo conectar. ¿Está corriendo el servidor?");
            System.exit(1);
            return;
        }

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));


        System.out.println(in.readLine());
        System.out.println(in.readLine());

        String input;
        while ((input = stdIn.readLine()) != null) {
            out.println(input);

            String respuesta;
            while ((respuesta = in.readLine()) != null && !respuesta.isEmpty()) {
                System.out.println(respuesta);
            }

            if (input.equalsIgnoreCase("bye")) break;
        }

        out.close();
        in.close();
        socket.close();
    }
}