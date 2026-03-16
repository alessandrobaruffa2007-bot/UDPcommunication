package client;

import java.net.*;
import java.util.Scanner;

public class MainClient {

    public static void main(String[] args) {

        try {

            InetAddress serverAddress = InetAddress.getLocalHost();
            int port = 3000;

            DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in);

            while (true) {

                System.out.print("Scrivi un messaggio: ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // invio messaggio
                byte[] bufferOut = message.getBytes();
                DatagramPacket outPacket =
                        new DatagramPacket(bufferOut, bufferOut.length, serverAddress, port);

                socket.send(outPacket);

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

            socket.close();
            scanner.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}