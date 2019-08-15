package client;

import java.net.MalformedURLException;
import java.rmi.*;
import java.io.*;
import java.rmi.server.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import models.*;
import protocol.Type;
import server.NullObjectException;
import server.RMIServerInterface;
import storage.Settings;


/**
 * Implementação da interface do cliente RMI.
 *
 * Os menus encontram-se todos no lado do cliente para quando o servidor rmi falhar, o failover
 * seja o mais transparente possivel. Em caso de falha de ligação (RemoteException) o cliente
 * fica em ciclo até conseguir efectuar a operação no lado do servidor. Desta forma o input
 * é pedido ao cliente uma só vez.
 */
public class RMIClient extends UnicastRemoteObject implements RMIClientInterface {

    private static final int TIMEOUT = 2500;
    private static String REGISTRY_URL;
    private static RMIServerInterface server;
    private static Utilizador utilizador;
    private static RMIClient client;

    /**
     * Construtor sem parametros
     *
     * @throws RemoteException Excepção de objecto remoto
     */
    public RMIClient() throws RemoteException {
        super();
    }

    /**
     * Função de entrada do cliente. Quando o utilizador abre o cliente, é mostrado
     * um menu simples de login / registo de conta.
     * @param args args
     */
    public static void main(String args[]) {
        Settings settings = new Settings();
        REGISTRY_URL = settings.getRegistryUrl();
        System.getProperties().put("java.security.policy", "policy.all");
        System.setSecurityManager(new RMISecurityManager());
        connectToRMIServer();

        boolean exitOption = false;
        while (!exitOption) {
            String message;
            String greet = "Ligado ao servidor rmi.\n1 - Register;\n2 - Login\n3 - Sair\n";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(input);
            try {
                do {
                    utilizador = null;
                    System.out.println(greet);
                    System.out.println("> ");
                    message = reader.readLine().trim();
                    switch (message) {
                        case "1":
                            register();
                            break;
                        case "2":
                            login();
                            break;
                        case "3":
                            exitOption = true;
                            break;
                        default:
                            System.out.println("Tente outra vez.\n");
                            break;
                    }
                    if (exitOption) {
                        System.out.println("Adeus.");
                        if (server != null && utilizador != null) {
                            server.unsubscribe(utilizador.getUsername());
                        }
                        System.exit(0);
                    }
                    if (utilizador != null) {
                        System.out.println(String.format("Bem vindo '%s'", utilizador.getUsername()));
                        server.subscribe(utilizador.getUsername(), client);
                        displayNotifications();
                        mainMenu();
                        utilizador = null;
                    }
                } while (utilizador == null);
            } catch (RemoteException e) {
                connectToRMIServer();
            } catch (IOException e) {
                System.out.println("Tente outra vez");
            }
        }
    }

    /**
     * Menu de login. Só é pedido username e password
     *
     */
    private static void login() {
        String username, password;
        System.out.print("Username: ");
        username = getInputData().trim();
        System.out.print("Password: ");
        password = getInputData().trim();
        boolean error;
        do {
            error = false;
            try {
                utilizador = server.login(client, username, password);
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Menu de registo de conta. Só sao pedidos username e password
     *
     */
    private static void register() {
        String username, password;
        System.out.print("Username: ");
        username = getInputData().trim();
        System.out.print("Password: ");
        password = getInputData().trim();
        boolean error;
        do {
            error = false;
            try {
                utilizador = server.register(client, username, password);
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Menu principal do cliente onde são dadas as escolher todas as operações que o
     * utilizador tem permissão para fazer.
     * As opções que requerem permissao de editor só são mostradas a quem tem permissao
     * para as fazer.
     */
    private static void mainMenu() {
        String message;
        boolean exitOption = false;

        String options[] = {
                "[Editor] Menu adicionar",                                  // 1
                "[Editor] Menu editar",                                     // 2
                "[Editor] Menu remover",                                    // 3
                "[Editor] Dar privilégios de editor a um utilizador",       // 4
                "Listar todos os artistas disponíveis",                     // 5
                "Pesquisar artista",                                        // 6
                "Pesquisar albúm",                                          // 7
                "Pesquisar música",                                         // 8
                "Escrever crítica a um álbum",                              // 9
                "Editar album",                                             // 10
                "Editar a descrição de um album",                           // 11
                "Upload de músicas",                                        // 12
                "Download de músicas",                                      // 13
                "Partilha de ficheiros musicais com outros utilizadores",   // 14
                "Sair",                                                     // 15
        };


        do {
            StringBuilder menuString = new StringBuilder("Main Menu");
            for (int i = utilizador.getIsEditor() ? 0 : 4; i < options.length; i++) {
                menuString.append(String.format("\n%d - %s", i + 1, options[i]));
            }

            System.out.println(menuString);
            System.out.println("> ");
            message = getInputData().trim();
            switch (message.trim()) {
                case "5":
                    listArtist();
                    break;
                case "6":
                    searchArtist();
                    break;
                case "7":
                    searchAlbum();
                    break;
                case "8":
                    searchMusic();
                    break;
                case "9":
                    createCritique();
                    break;
                case "10":
                    updateAlbum();
                    break;
                case "11":
                    editAlbumDesc();
                    break;
                case "12":
                    uploadSong();
                    break;
                case "13":
                    downloadSong();
                    break;
                case "14":
                    shareSong();
                    break;
                case "15":
                    exitOption = true;
                    try {
                        server.unsubscribe(utilizador.getUsername());
                    } catch (RemoteException ignored) {
                    }
                    break;
                case "1":
                    if (utilizador.getIsEditor()) {
                        addMenu();
                        break;
                    }
                case "2":
                    if (utilizador.getIsEditor()) {
                        editMenu();
                        break;
                    }
                case "3":
                    if (utilizador.getIsEditor()) {
                        removeMenu();
                        break;
                    }
                case "4":
                    if (utilizador.getIsEditor()) {
                        makeEditor();
                        break;
                    }
                case "r":
                    break;
                default:
                    System.out.println("Erro, tente outra vez\n");
                    break;
            }
        } while (!exitOption);
        System.out.println("Adeus.");
        try {
            server.unsubscribe(utilizador.getUsername());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Menu de criação de entidades. A partir deste menu o editor escolhe
     * se quer criar um novo artista, album ou música.
     */
    private static void addMenu() {
        String message;
        boolean exitOption = false;
        String[] options = {
                "[Editor] Adicionar artista",                               // 1
                "[Editor] Adicionar album",                                 // 2
                "[Editor] Adicionar música",                                // 3
                "Retornar"
        };

        StringBuilder menuString = new StringBuilder("Menu adicionar");
        for (int i = 0; i < options.length; i++) {
            menuString.append(String.format("\n%d - %s", i + 1, options[i]));
        }

        do {
            System.out.println(menuString);
            System.out.println("> ");
            message = getInputData().trim();
            switch (message) {
                case "1":
                    createArtist();
                    break;
                case "2":
                    createAlbum();
                    break;
                case "3":
                    createSong();
                    break;
                case "12":
                case "4":
                    exitOption = true;
                    break;
                default:
                    System.out.println("Erro, tente outra vez\n");
                    break;
            }
        } while (!exitOption);
    }

    /**
     * Menu de edição de entidades já existentes.
     */
    private static void editMenu() {
        String message;
        boolean exitOption = false;
        String[] options = {
                "[Editor] Editar música",                                // 1
                "Retornar"                                               // 2
        };

        StringBuilder menuString = new StringBuilder("Menu editar");
        for (int i = 0; i < options.length; i++) {
            menuString.append(String.format("\n%d - %s", i + 1, options[i]));
        }

        do {
            System.out.println(menuString);
            System.out.println("> ");
            message = getInputData().trim();
            switch (message) {
                case "1":
                    editSong();
                    break;
                case "2":
                    exitOption = true;
                    break;
                default:
                    System.out.println("Erro, tente outra vez\n");
                    break;
            }
        } while (!exitOption);
    }

    /**
     * Menu de remoção de artistas, albuns e musicas.
     */
    private static void removeMenu() {
        String message;
        boolean exitOption = false;
        String[] options = {
                "[Editor] Remover artista",                               // 1
                "[Editor] Remover album",                                 // 2
                "[Editor] Remover música",                                // 3
                "Retornar"
        };

        StringBuilder menuString = new StringBuilder("Menu remover");
        for (int i = 0; i < options.length; i++) {
            menuString.append(String.format("\n%d - %s", i + 1, options[i]));
        }

        do {
            System.out.println(menuString);
            System.out.println("> ");
            message = getInputData().trim();
            switch (message) {
                case "1":
                    removeArtist();
                    break;
                case "2":
                    removeAlbum();
                    break;
                case "3":
                    removeSong();
                    break;
                case "4":
                    exitOption = true;
                    break;
                default:
                    System.out.println("Erro, tente outra vez\n");
                    break;
            }
        } while (!exitOption);
    }


    /**
     * Função auxiliar para obter input de teclado do utilizador
     * Os caracteres ';' e '|' não são permitidos visto que sao usados
     * como separadores no protocolo utilizado internamente sobre multicast.
     *
     * @return input em formato String
     */
    private static String getInputData() {
        boolean error = false;
        String data = null;
        CharSequence test_1 = ";";
        CharSequence test_2 = "|";
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        do {
            if (error) {
                System.out.println("Erro, tente outra vez. (';' e '|' não são permitidos)");
                error = false;
            }
            try {
                data = reader.readLine().trim();
            } catch (IOException e) {
                error = true;
            }
            if (data == null || data.contains(test_1) || data.contains(test_2) || data.isEmpty()) {
                error = true;
            }
        } while (error);
        return data;
    }

    /**
     * Menu de transferência de ficheiro da máquina do utilizador para um servidor multicast.
     * É pedido o nome da música e do ficheiro.
     * <p>
     * É iniciado um cliente TCP que se liga ao endereço e porto retornado pelo servidor
     * e a transferencia começa.
     */
    private static void uploadSong() {
        String songName, fileName, address;
        int port;
        System.out.print("Nome da música: ");
        songName = getInputData().trim();
        System.out.print("Nome do ficheiro: ");
        fileName = getInputData().trim();
        boolean error;
        do {
            error = false;
            try {
                HashMap<String, String> map = server.uploadSong(songName, utilizador.getUsername());
                if (map.get("type").equals(String.valueOf(Type.SUCCESS))) {
                    address = map.get("address");
                    port = Integer.parseInt(map.get("port"));
                    TCPClient tcpClient = new TCPClient(address, port, fileName);
                    tcpClient.sendFile();
                } else {
                    System.out.println(map.get("msg"));
                }
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Menu de transferencia de ficheiros de musica do servidor para a máquina do cliente.
     * <p>
     * É mostrada uma lista de músicas disponiveis para download e o utilizador escolhe qual quer transferir.
     */
    private static void downloadSong() {
        String songName, address;
        int port;
        ArrayList<Musica> musicas = null;
        boolean error;
        do {
            error = false;
            try {
                musicas = server.getDownloadableSongs(utilizador.getUsername());
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);

        if (musicas != null && musicas.size() > 0) {
            for (int i = 0; i < musicas.size(); i++) {
                System.out.println(String.format("%d - %s", i, musicas.get(i).getTitulo()));
            }

            int index;
            do {
                System.out.print(String.format("Música para transferir (%d a %d): ", 0, musicas.size()));
                index = Integer.parseInt(getInputData().trim());
            } while (index > musicas.size() || index < 0);
            songName = musicas.get(index).getTitulo();
            do {
                error = false;
                try {
                    HashMap<String, String> map = server.downloadSong(songName, utilizador.getUsername());
                    if (map.get("type").equals(String.valueOf(Type.SUCCESS))) {
                        address = map.get("address");
                        port = Integer.parseInt(map.get("port"));
                        TCPClient tcpClient = new TCPClient(address, port, songName);
                        tcpClient.downloadFile();
                    } else {
                        System.out.println(map.get("msg"));
                    }
                } catch (RemoteException e) {
                    error = true;
                    connectToRMIServer();
                }
            } while (error);
        } else {
            System.out.println("Não existem músicas disponiveis para download.");
        }

    }

    /**
     * Menu de partilha de ficheiro de música com outros utilizadores.
     * O utilizador escolhe de uma lista de músicas para partilhar e depois insere o nome de utilizador com quem quer
     * partilhar. Se o utilizador nao existir, é mostrada uma mensagem de erro.
     */
    private static void shareSong() {
        String songName, username;
        ArrayList<Musica> musicas = null;
        boolean error;
        do {
            error = false;
            try {
                musicas = server.getDownloadableSongs(utilizador.getUsername());
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);

        if (musicas != null && musicas.size() > 0) {
            for (int i = 0; i < musicas.size(); i++) {
                System.out.println(String.format("%d - %s", i, musicas.get(i).getTitulo()));
            }

            int index;
            do {
                System.out.print(String.format("Música para partilhar (%d a %d): ", 0, musicas.size()));
                index = Integer.parseInt(getInputData().trim());
            } while (index > musicas.size() || index < 0);
            songName = musicas.get(index).getTitulo();


            System.out.print("Nome do utilizador com quem partilhar o ficheiro: ");
            username = getInputData().trim();

            do {
                error = false;
                try {
                    String message = server.shareSong(songName, username);
                    System.out.println(message);
                } catch (RemoteException e) {
                    error = true;
                    connectToRMIServer();
                }
            } while (error);
        } else {
            System.out.println("Não existem músicas disponiveis para partilhas.");
        }
    }

    /**
     * Menu de edição da descricao de um album.
     * O utilizador escolhe o album a editar e escreve a nova descriçao
     * Os editores anteriores sao notificados da alteração.
     */
    private static void editAlbumDesc() {
        String album, desc, message;
        System.out.println("Nome do album a editar: ");
        album = getInputData();
        System.out.println("Nova descrição: ");
        desc = getInputData();
        boolean error;
        do {
            error = false;
            try {
                message = server.editAlbumDesc(album, desc, utilizador.getUsername());
                System.out.println(message);
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Metodo para editar o nome e genero de um album
     */
    private static void updateAlbum() {
        String album, genero, newtitle;
        Album aux_album = null;
        System.out.println("Nome do album a editar: ");
        album = getInputData();
        boolean error;
        do {
            error = false;
            try {
                aux_album = server.searchAlbum(album);
                System.out.println(aux_album.getTitulo() + " found");
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException | ExceedsMaxLengthException e) {
                System.out.println(e.getMessage());
            }
        } while (error);

        if (aux_album != null) {
            System.out.println(String.format("titulo atual: '%s'\nNovo tíulo do album: ", aux_album.getTitulo()));
            newtitle = getInputData().trim();
            System.out.println(String.format("género musical atual: '%s'\nNovo género musical: ", aux_album.getGeneroMusical()));
            genero = getInputData().trim();

            do {
                error = false;
                try {
                    System.out.println(server.updateAlbum(aux_album.getTitulo(), newtitle, genero));
                } catch (RemoteException e) {
                    error = true;
                    connectToRMIServer();
                } catch (NullObjectException e) {
                    System.out.println(e.getMessage());
                }
            } while (error);
        }
    }

    /*
     *Menu de remoçao de artista. Apenas é pedido o nome do artista a remover.
     */
    private static void removeArtist() {
        String artista;
        System.out.println("Nome do artista a remover: ");
        artista = getInputData();
        boolean error;
        do {
            error = false;
            try {
                server.removeArtist(artista);
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Menu de remoção de album.
     */
    private static void removeAlbum() {
        String album;
        System.out.println("Nome do album a remover: ");
        album = getInputData();
        boolean error;
        do {
            error = false;
            try {
                server.removeAlbum(album);
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Menu de remoçao de música.
     */
    private static void removeSong() {
        String musica;
        System.out.println("Nome da música a remover: ");
        musica = getInputData();
        boolean error;
        do {
            error = false;
            try {
                 server.removeSong(musica);
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Menu de dar privilégios de editor a outro utilizador.
     * O utilizador é sempre notificado das novas permissões estando ele online ou offline.
     */
    private static void makeEditor() {
        String username;
        System.out.println("Nome do utilizador a tornar editor: ");
        username = getInputData();
        boolean error;
        do {
            error = false;
            try {
                String message = server.makeEditor(username);
                System.out.println(message);
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Menu de criação de avaliação de um album.
     */
    private static void createCritique() {
        String titulo;
        String critica;
        String pontuacao = null;
        boolean error;


        System.out.println("Nome do albúm a avaliar: ");
        titulo = getInputData().trim();

        do {
            error = false;
            try {
                System.out.println("Pontuacao do album: (0-100%): ");
                pontuacao = getInputData().trim();
            } catch (NumberFormatException e) {
                error = true;
                System.out.println("0-100%");
            }
        } while (error);

        System.out.println("Critica (max 300 char): ");
        critica = getInputData();


        do {
            error = false;
            try {
                Critica aux_critica = server.createCritique(titulo, pontuacao, critica);
                System.out.println("Critica criada com sucesso.");
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException | ExceedsMaxLengthException e) {
                System.out.println(e.getMessage());
            }
        } while (error);
    }

    /**
     * Menu de procura de música.
     * É pedido o nome da música a procurar.
     */
    private static void searchMusic() {
        Musica musica;
        String data;
        System.out.println("Nome da música a procurar: ");
        data = getInputData().trim();
        boolean error;
        do {
            error = false;
            try {
                musica = server.searchMusic(data);
                System.out.println(musica.getTitulo());
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException e) {
                System.out.println(e.getMessage());
            }
        } while (error);
    }

    /**
     * Menu de procura de album.
     * Sao mostrados todos os detalhes do album encontrado.
     */
    private static void searchAlbum() {
        String album;
        System.out.println("Nome do album a procurar: ");
        album = getInputData().trim();
        Album aux_album;
        String message;
        boolean error;
        do {
            error = false;
            try {
                aux_album = server.searchAlbum(album);
                System.out.println(String.format("Titulo: %s\nAutor: %s\nGenero Musical: %s\nMusicas:", aux_album.getTitulo(), aux_album.getAutor().getNome(),
                        aux_album.getGeneroMusical()));
                for (Musica i : aux_album.getMusicas()) {
                    System.out.println(String.format("- %s", i.getTitulo()));
                }
                System.out.println("Criticas: ");
                for (Critica i : aux_album.getCriticas()) {
                    System.out.println(String.format("Pontuação: %d\nJustificação: %s", i.getPontuacao(), i.getJustificacao()));
                }

            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException | ExceedsMaxLengthException e) {
                System.out.println(e.getMessage());
            }
        } while (error);
    }


    /**
     * Método para edição de uma musica.
     * É pedido um album ao utilizador e é lhe mostrado uma lista de musicas.
     * O utilizador depois escolhe a musica a editar e qual será o novo nome.
     */
    private static void editSong() {
        String albumName;
        System.out.println("Album da musica a editar: ");
        albumName = getInputData().trim();
        Album album = null;
        ArrayList<Musica> musicas = null;
        boolean error;
        do {
            error = false;
            try {
                album = server.searchAlbum(albumName);
                musicas = album.getMusicas();
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException | ExceedsMaxLengthException e) {
                System.out.println(e.getMessage());
            }
        } while (error);

        if (album != null && album.getMusicas().size() > 0) {
            for (int i = 0; i < musicas.size(); i++) {
                System.out.println(String.format("%d - %s", i, musicas.get(i).getTitulo()));
            }

            int index;
            do {
                System.out.print(String.format("Música a editar (%d a %d): ", 0, musicas.size()));
                index = Integer.parseInt(getInputData().trim());
            } while (index > musicas.size() || index < 0);
            String songName = musicas.get(index).getTitulo();
            System.out.println("Novo nome: ");
            String newName = getInputData().trim();
            do {
                error = false;
                try {
                    server.editSong(songName, newName);
                } catch (RemoteException e) {
                    error = true;
                    connectToRMIServer();
                }
            } while (error);
        } else {
            System.out.println("Não existem músicas disponiveis para editar.");
        }
    }

    /**
     * Menu que mostra a listagem de todos os artistas existentes.
     */
    private static void listArtist() {
        ArrayList<Artista> artistas;
        boolean error;
        do {
            error = false;
            try {
                artistas = server.listArtist();
                System.out.println("Listando todos os artistas.");
                for (Artista i : artistas) {
                    System.out.println(String.format("Nome: %s\nAlbuns: ", i.getNome()));
                    for (Album j : i.getAlbuns()) {
                        System.out.println(String.format("- %s", j.getTitulo()));
                    }
                    System.out.println("\n");
                }
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException e) {
                System.out.println(e.getMessage());
            }
        } while (error);
    }

    /**
     * Menu de criação de uma música.
     */
    private static void createSong() {
        String songName;
        String album;
        System.out.println("Nome da musica: ");
        songName = getInputData();
        System.out.println("Nome do album: ");
        album = getInputData();

        boolean error;
        do {
            error = false;
            try {
                Musica musica = server.createSong(songName, album);
                System.out.println(musica.getTitulo());
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException e) {
                System.out.println(e.getMessage());
            }
        } while (error);
    }

    /**
     * Menu de criação de um artista.
     */
    private static void createArtist() {
        String nome;
        System.out.println("Nome do artista a criar: ");
        nome = getInputData();
        boolean error;
        do {
            error = false;
            try {
                Artista artista = server.createArtist(nome);
                System.out.println(artista.getNome());
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException e) {
                System.out.println(e.getMessage());
            }
        } while (error);
    }

    /**
     * Menu de criação de um album.
     * Necessário nome do album a inserir, o nome do artista do album e o genero musical.
     */
    private static void createAlbum() {
        String[] parameters = new String[3];
        System.out.println("Nome do album: ");
        parameters[0] = getInputData();
        System.out.println("Nome do artista: ");
        parameters[1] = getInputData();
        System.out.println("Nome do genero musical: ");
        parameters[2] = getInputData();

        boolean error;
        do {
            error = false;
            try {
                Album album = server.createAlbum(parameters[0], parameters[1], parameters[2]);
                System.out.println(album.getTitulo());
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException e) {
                System.out.println(e.getMessage());
            }
        } while (error);
    }

    /**
     * Menu de procura de artista.
     * Sao tambem mostrados os albuns do artista encontrado.
     */
    private static void searchArtist() {
        Artista artista;
        System.out.println("Nome do artista a procurar.");
        System.out.println("> ");
        String nome = getInputData().trim();

        boolean error;
        do {
            error = false;
            try {
                artista = server.searchArtista(nome);
                System.out.println(String.format("Artista: %s", artista.getNome()));
                for (Album i : artista.getAlbuns()) {
                    System.out.println(String.format("- %s | %s", i.getTitulo(), i.getGeneroMusical()));
                }
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            } catch (NullObjectException e) {
                System.out.println(e.getMessage());
            }
        } while (error);
    }

    /**
     * Função auxiliar para a reconnecção do cliente ao servidor RMI.
     * Sempre que a ligação é quebrada, o cliente fica num ciclo a tentar ligar a outro servidor.
     */
    private static void connectToRMIServer() {
        boolean error;
        System.out.print("A estabelecer ligação");
        do {
            System.out.print(".");
            error = false;
            try {
                client = new RMIClient();
                server = (RMIServerInterface) Naming.lookup(REGISTRY_URL + "server");
                if (server.isAlive() && utilizador != null) {
                    server.subscribe(utilizador.getUsername(), client);
                }
            } catch (NotBoundException | MalformedURLException | RemoteException e1) {
                error = true;
                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    System.out.println("Retry...");
                }
            }
        } while (error);
        System.out.println("!");
    }

    /**
     * Função auxiliar que mostra todas as notificaçoes que foram enviadas a um utilizador quando
     * este se encontrava offline.
     */
    private static void displayNotifications() {
        boolean error;
        do {
            error = false;
            try {
                ArrayList<String> notifications = server.getNotifications(utilizador.getUsername());
                if (notifications.size() > 0) {
                    System.out.println("Novas notificações.");
                } else {
                    System.out.println("Sem novas notificações.");
                }
                for (String not : notifications) {
                    System.out.println(not);
                }
            } catch (RemoteException e) {
                error = true;
                connectToRMIServer();
            }
        } while (error);
    }

    /**
     * Funcao utilizada para atualizar as permissoes do utilizador atual.
     * Para poder ter acesso aos novos menus quando este passa a ser editor.
     *
     * @throws RemoteException excepção se a ligação com o servidor falhar
     */
    @Override
    public void givePermission() throws RemoteException {
        utilizador.setIsEditor(true);
    }

    /**
     * Função para imprimir notificações em tempo real no ecra do utilizador atual.
     *
     * @param message notificação a mostrar
     * @throws RemoteException excepção se a ligação falhar
     */
    @Override
    public void print(String message) throws RemoteException {
        System.out.println(message);
    }
}
