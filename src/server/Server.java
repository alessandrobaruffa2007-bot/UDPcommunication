package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Classe {@code Server} che implementa un server UDP echo.
 * <p>
 * Il server rimane in ascolto sulla porta specificata, riceve
 * un {@link DatagramPacket} dal client e risponde con lo stesso
 * messaggio ricevuto (comportamento echo).
 * </p>
 * <p>
 * Implementa {@link AutoCloseable} per consentire l'uso nel
 * costrutto <em>try-with-resources</em>, che garantisce la
 * chiusura automatica del socket al termine del blocco.
 * </p>
 *
 * @author UDPCommunication
 * @version 1.0
 */
public class Server implements AutoCloseable {

    /** Porta su cui il server rimane in ascolto. */
    public static final int PORT = 12345;

    /**
     * Dimensione massima (in byte) del buffer per i pacchetti in ricezione.
     * <p>
     * Valore scelto di 8192 byte: abbondantemente sufficiente per messaggi
     * di testo e ben al di sotto del limite pratico UDP su Ethernet
     * (~1472 byte senza frammentazione, 65507 byte come massimo assoluto).
     * </p>
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Flag che controlla il ciclo principale del server.
     * Impostato a {@code false} tramite {@link #stop()} per terminare
     * ordinatamente il loop di ricezione.
     */
    private volatile boolean running = false;

    /**
     * Socket UDP del server.
     * Dichiarato {@code final} perché assegnato una sola volta nel costruttore.
     */
    private final DatagramSocket socket;

    /**
     * Costruisce un nuovo {@code Server} aprendo un {@link DatagramSocket}
     * sulla porta {@value #PORT}.
     *
     * @throws SocketException se la porta è già in uso o non è possibile
     *                         aprire il socket.
     */
    public Server() throws SocketException {
        socket = new DatagramSocket(PORT);
        System.out.printf("[Server] In ascolto sulla porta %d...%n", PORT);
    }

    /**
     * Avvia il ciclo di ricezione/risposta del server.
     * <p>
     * Il server rimane in ascolto in modo continuo:
     * <ol>
     *   <li>Riceve un {@link DatagramPacket} dal client.</li>
     *   <li>Estrae il messaggio di testo dal pacchetto.</li>
     *   <li>Invia lo stesso messaggio (echo) all'indirizzo e alla porta
     *       del mittente.</li>
     * </ol>
     * </p>
     */
    public void start() {
        // RECEIVE_BUFFER.getBytes() fornisce il buffer di ricezione senza dichiarare un array esplicito
        running = true;
        while (running) {
            // --- Ricezione del pacchetto dal client ---
            DatagramPacket receivePacket = new DatagramPacket(
                    new byte[BUFFER_SIZE],
                    BUFFER_SIZE
            );

            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                System.err.printf("[Server] Errore durante la ricezione del pacchetto: %s%n", e.getMessage());
                continue; // Tenta di ricevere il pacchetto successivo
            }

            // Estrae il messaggio ricevuto convertendo i byte in stringa
            String message = new String(
                    receivePacket.getData(),
                    0,
                    receivePacket.getLength()
            );

            System.out.printf("[Server] Messaggio ricevuto da %s:%d → \"%s\"%n",
                    receivePacket.getAddress().getHostAddress(),
                    receivePacket.getPort(),
                    message);

            // --- Invio dell'echo al client ---
            // getBytes() converte il messaggio in byte direttamente inline
            DatagramPacket sendPacket = new DatagramPacket(
                    message.getBytes(),
                    message.getBytes().length,
                    receivePacket.getAddress(), // indirizzo IP del mittente
                    receivePacket.getPort()      // porta del mittente
            );

            try {
                socket.send(sendPacket);
                System.out.printf("[Server] Echo inviato a %s:%d%n",
                        receivePacket.getAddress().getHostAddress(),
                        receivePacket.getPort());
            } catch (IOException e) {
                System.err.printf("[Server] Errore durante l'invio dell'echo: %s%n", e.getMessage());
            }
        }
    }

    /**
     * Ferma il ciclo di ricezione del server impostando il flag {@code running}
     * a {@code false}. Il loop terminerà al completamento dell'iterazione corrente.
     */
    public void stop() {
        running = false;
    }

    /**
     * Ferma il server e chiude il {@link DatagramSocket} rilasciando la porta.
     * <p>
     * Viene invocato automaticamente dal costrutto <em>try-with-resources</em>
     * al termine del blocco, garantendo la liberazione della risorsa anche
     * in caso di eccezione.
     * </p>
     */
    @Override
    public void close() {
        stop();
        socket.close();
        System.out.printf("[Server] Socket chiuso.%n");
    }
}
