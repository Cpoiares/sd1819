package server;

import models.*;
import storage.FicheiroDeObjectos;
import storage.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe principal do servidor multicast.
 * Cria uma multicast socket e cria uma instancia de {@link server.ClientHandler} para
 * lidar com cada pedido recebido.
 */
public class MulticastServer {
    public final static int MAX_BUFFER_SIZE = 8192;
    final static String ARTISTAS_FILE = "artistas.bin";
    final static String UTILIZADORES_FILE = "utilizadores.bin";
    static int MULTICAST_PORT;
    static String MULTICAST_ADDRESS;


    /**
     * Método principal do servidor. Criar socket, quando um clente se liga
     * é criado um handler para ligar com o pedido.
     *
     * @param args string
     */
    public static void main(String[] args) {

        Settings settings = new Settings();
        MULTICAST_ADDRESS = settings.getMulticastAddress();
        MULTICAST_PORT = settings.getMulticastPort();
        HashMap<String, ArrayList> offlineNotifications = new HashMap<>();

        CopyOnWriteArrayList<Utilizador> utilizadores = null;
        CopyOnWriteArrayList<Artista> artistas = null;

        if (artistas == null) {
            artistas = new CopyOnWriteArrayList<>();
        }
        if (utilizadores == null) {
            utilizadores = new CopyOnWriteArrayList<>();
        }

        /* load from file storage */
        FicheiroDeObjectos storage = new FicheiroDeObjectos();
        try {
            storage.abreLeitura(ARTISTAS_FILE);
            artistas = (CopyOnWriteArrayList<Artista>) storage.leObjecto();
            storage.fechaLeitura();
            storage.abreLeitura(UTILIZADORES_FILE);
            utilizadores = (CopyOnWriteArrayList<Utilizador>) storage.leObjecto();
            storage.fechaLeitura();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro a carregar ficheiros de objectos.");
            System.out.println("Dados de teste");
            Artista test_a = new Artista("testA");
            for (int i = 0; i < 5; i++) {
                Album album = new Album(String.format("a_%d", i), test_a, "rock");
                test_a.addAlbum(album);
            }
            Artista test_b = new Artista("testB");
            for (int i = 0; i < 5; i++) {
                Album album = new Album(String.format("b_%d", i), test_b, "rock");
                test_b.addAlbum(album);
            }
            Artista test_c = new Artista("testC");
            for (int i = 0; i < 5; i++) {
                Album album = new Album(String.format("c_%d", i), test_a, "rock");
                test_c.addAlbum(album);
            }

            artistas.add(test_a);
            artistas.add(test_b);
            artistas.add(test_c);
        }


        /* start test data */

        /*Artista a = new Artista("A");
        for (int i = 0; i < 5; i++) {
            Album album = new Album(String.format("%d", i), a, "rock");
            a.addAlbum(album);
        }
        artistas.add(a);
        */
        /* end test data */


        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(MULTICAST_PORT);  // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[MulticastServer.MAX_BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                ClientHandler clientHandler = new ClientHandler(packet, utilizadores, artistas, offlineNotifications);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}