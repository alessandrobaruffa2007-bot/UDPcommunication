package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Classe {@code Client} che implementa un client UDP per comunicare
 * con il server echo.
 * <p>
 * Il client:
 * <ol>
 *   <li>Invia un messaggio di testo al server tramite {@link DatagramPacket}.</li>
 *   <li>Attende la risposta echo dal server.</li>
 *   <li>Stampa il messaggio di risposta ricevuto.</li>
 * </ol>
 * </p>
 * <p>
 * Implementa {@link AutoCloseable} per consentire l'uso nel costrutto
 * <em>try-with-resources</em>, che garantisce la chiusura automatica
 * del socket al termine del blocco.
 * </p>
 *
 * @author UDPCommunication
 * @version 1.0
 */
public class Client implements AutoCloseable {

    /** Indirizzo IP (o hostname) del server a cui connettersi. */
    private static final String SERVER_HOST = "localhost";

    /** Porta del server a cui inviare i messaggi. */
    private static final int SERVER_PORT = 12345;

    /**
     * Dimensione massima (in byte) del buffer per i pacchetti in ricezione.
     * <p>
     * Valore scelto di 4096 byte: abbondantemente sufficiente per messaggi
     * di testo e ben al di sotto del limite pratico UDP su Ethernet
     * (~1472 byte senza frammentazione, 65507 byte come massimo assoluto).
     * </p>
     */
    private static final int BUFFER_SIZE = 4096;

    /** Timeout in millisecondi per la ricezione della risposta del server. */
    private static final int TIMEOUT_MS = 5000;

    /**
     * Socket UDP del client.
     * Dichiarato {@code final} perché assegnato una sola volta nel costruttore.
     */
    private final DatagramSocket socket;

    /** Indirizzo IP risolto del server. */
    private final InetAddress serverAddress;

    /**
     * Costruisce un nuovo {@code Client} aprendo un {@link DatagramSocket}
     * su una porta casuale assegnata dal sistema operativo e risolvendo
     * l'indirizzo del server.
     *
     * @throws SocketException      se non è possibile creare il socket UDP.
     * @throws UnknownHostException se l'hostname del server non è risolvibile.
     */
    public Client() throws SocketException, UnknownHostException {
        // Porta 0 → il SO assegna automaticamente una porta libera
        socket = new DatagramSocket();
        // Imposta un timeout per evitare attese infinite in caso di mancata risposta
        socket.setSoTimeout(TIMEOUT_MS);
        // Risolve l'hostname del server in un InetAddress
        serverAddress = InetAddress.getByName(SERVER_HOST);
        System.out.printf("[Client] Socket creato. Connessione verso %s:%d%n", SERVER_HOST, SERVER_PORT);
    }

    /**
     * Invia il messaggio specificato al server e attende l'eco di risposta.
     * <p>
     * In caso di timeout o errore di I/O viene stampato un messaggio
     * di errore descrittivo senza terminare il programma.
     * </p>
     *
     * @param message il testo da inviare al server; non deve essere {@code null}.
     */
    public void sendMessage(String message) {
        if (message == null || message.isEmpty()) {
            System.err.printf("[Client] Il messaggio non può essere nullo o vuoto.%n");
            return;
        }

        // --- Preparazione e invio del pacchetto ---
        // getBytes() converte il messaggio in byte direttamente inline
        DatagramPacket sendPacket = new DatagramPacket(
                message.getBytes(),
                message.getBytes().length,
                serverAddress,
                SERVER_PORT
        );

        try {
            socket.send(sendPacket);
            System.out.printf("[Client] Messaggio inviato: \"%s\"%n", message);
        } catch (IOException e) {
            System.err.printf("[Client] Errore durante l'invio del messaggio: %s%n", e.getMessage());
            return; // Non ha senso attendere una risposta se l'invio è fallito
        }

        // --- Ricezione dell'echo dal server ---
        DatagramPacket receivePacket = new DatagramPacket(
                new byte[BUFFER_SIZE],
                BUFFER_SIZE
        );

        try {
            socket.receive(receivePacket);
            // Converte i byte ricevuti nella stringa del messaggio
            String echo = new String(
                    receivePacket.getData(),
                    0,
                    receivePacket.getLength()
            );
            System.out.printf("[Client] Echo ricevuto dal server: \"%s\"%n", echo);
        } catch (SocketTimeoutException e) {
            // Il server non ha risposto entro il timeout impostato
            System.err.printf("[Client] Timeout: nessuna risposta dal server entro %d ms.%n", TIMEOUT_MS);
        } catch (IOException e) {
            System.err.printf("[Client] Errore durante la ricezione dell'echo: %s%n", e.getMessage());
        }
    }

    /**
     * Chiude il {@link DatagramSocket} del client rilasciando le risorse.
     * <p>
     * Viene invocato automaticamente dal costrutto <em>try-with-resources</em>
     * al termine del blocco, garantendo la liberazione della risorsa anche
     * in caso di eccezione.
     * </p>
     */
    @Override
    public void close() {
        socket.close();
        System.out.printf("[Client] Socket chiuso.%n");
    }
}
