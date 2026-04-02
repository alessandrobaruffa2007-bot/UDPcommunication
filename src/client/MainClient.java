package client;

import java.net.*;
import java.util.Scanner;

public class MainClient {

    public static void main(String[] args) {

        try {

            InetAddress serverAddress = InetAddress.getLocalHost(); // trova indirrizzo server
            int port = 3000;

            DatagramSocket socket = new DatagramSocket(); //crea il socket(il ponte) dove inviare i pacchetti
            Scanner scanner = new Scanner(System.in); // ti permette di scrivere con la tastiera

            while (true) {

                System.out.print("Scrivi un messaggio: ");
                String message = scanner.nextLine();


                // invio messaggio
                byte[] bufferOut = message.getBytes(); // serializzazione
                DatagramPacket outPacket = new DatagramPacket(bufferOut, bufferOut.length, serverAddress, port); // crea un nuovo paccchetto per l'invio del  messaggio

                socket.send(outPacket); // invia il messaggio all'indirizzo e la porta

                // buffer per risposta
                byte[] bufferIn = new byte[256];
                DatagramPacket inPacket =
                        new DatagramPacket(bufferIn, bufferIn.length);

                // ricezione risposta
                socket.receive(inPacket);

                String response =
                        new String(inPacket.getData(), 0, inPacket.getLength());

                System.out.println("Risposta del server: " + response);
            }


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}