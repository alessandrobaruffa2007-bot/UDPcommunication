package server;

import java.net.*;

public class MainServer {

    public static void main(String[] args) {

        try {

            int port = 3000;
            DatagramSocket socket = new DatagramSocket(port);

            System.out.println("Server in ascolto sulla porta " + port);

            while (true) {

                // buffer di ricezione
                byte[] bufferIn = new byte[256];
                DatagramPacket inPacket = new DatagramPacket(bufferIn, bufferIn.length);

                // ricezione messaggio dal client
                socket.receive(inPacket);

                String message = new String(inPacket.getData(), 0, inPacket.getLength());
                System.out.println("Messaggio ricevuto dal client: " + message);

                // indirizzo e porta del client
                InetAddress clientAddress = inPacket.getAddress();
                int clientPort = inPacket.getPort();

                // risposta del server
                String response = "Ricevuto: " + message;
                byte[] bufferOut = response.getBytes();

                DatagramPacket outPacket =
                        new DatagramPacket(bufferOut, bufferOut.length, clientAddress, clientPort);

                // invio risposta
                socket.send(outPacket);
            }

        } catch (Exception e) {
            System.err.println("Error");
        }
    }
}