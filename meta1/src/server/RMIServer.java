package server;

import client.RMIClientInterface;
import models.*;
import protocol.Protocol;
import protocol.Type;
import storage.Settings;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implementação da interface do servidor RMI
 */

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
    private static final long serialVersionUID = 1L;

    private static String MULTICAST_ADDRESS;
    private static int MULTICAST_PORT;
    private static String REGISTRY_URL;
    private static int RMI_TIMEOUT;
    private static int MAX_RETRIES = 5;
    private static int SOCKET_TIMEOUT;
    private static MulticastSocket socket;
    private static InetAddress group;
    private static ConcurrentHashMap<String, RMIClientInterface> clients;


    /**
     * Construtor sem parâmetros
     * Set dos atributos através da class Settings (Atributos do servidor RMI)
     *
     * @throws RemoteException Excepção de objecto remoto
     */

    private RMIServer() throws RemoteException {
        super();

        Settings settings = new Settings();
        MULTICAST_ADDRESS = settings.getMulticastAddress();
        MULTICAST_PORT = settings.getMulticastPort();
        REGISTRY_URL = settings.getRegistryUrl();
        RMI_TIMEOUT = settings.getRmiTimeout();
        SOCKET_TIMEOUT = settings.getSocketTimeout();
        MAX_RETRIES = settings.getRmiRetries();

    }

    /**
     * Função de entrada do servidor RMI, este inicializado como servidor secundário,
     * verifica a disponibilidade do servidor primário através da função isAlive. Ao fim de MAX_RETRIES
     * este assume que não existe servidor primário e assume a sua posição, dando bind ao URL correspondente.
     * Sendo o servidor primário, cria uma socket no porto MULTICAST_PORT e junta-se ao grupo de endereço MULTICAST_ADDRESS
     *
     * @param args args
     */

    public static void main(String[] args) {
        clients = new ConcurrentHashMap<>();
        RMIServerInterface server;
        RMIServerInterface primaryServer;
        try {
            server = new RMIServer();

            try {
                Naming.bind(REGISTRY_URL + "server", server);
            } catch (AlreadyBoundException e) {
                System.out.println("A tornar secundario...");

                int retries = 0;
                while (retries < MAX_RETRIES) {
                    try {
                        primaryServer = (RMIServerInterface) Naming.lookup(REGISTRY_URL + "server");
                        if (primaryServer.isAlive()) {
                            retries = 0;
                        }
                    } catch (RemoteException | NotBoundException | NullPointerException e2) {
                        System.out.println(String.format("Ligação com o servidor primário falhou (%d/%d)", retries + 1, MAX_RETRIES));
                        retries++;
                    }
                    try {
                        Thread.sleep(RMI_TIMEOUT);
                    } catch (InterruptedException e1) {
                        System.out.println("Sleep interrupted.");
                    }
                }
                System.out.println("A tornar primario...");
                Naming.rebind(REGISTRY_URL + "server", server);
            }
            socket = new MulticastSocket(MULTICAST_PORT);  // create socket and bind it
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("RMI server shutting down...");
            System.exit(1);
        }
        System.out.println("RMI server ready...");
    }

    /**
     * Método para a subscrição de um cliente RMICLient ao servidor.
     * O objecto Cliente é inserido no ConcurrentHashMap de clientes.
     *
     * @param name username
     * @param client referencia do cliente
     */
    @Override
    public void subscribe(String name, RMIClientInterface client) {
        System.out.println(name + " subscribed.");
        clients.put(name, client);
    }

    /**
     * Método de unsubcribe de um RMIClient.
     * O objecto Cliet
     *
     * @param name username  a remover
     */
    @Override
    public void unsubscribe(String name) {
        System.out.println(name + " unsubscribed.");
        clients.remove(name);
    }

    /**
     * Método auxiliar para enumeração de utilizadores.
     * @return ArrayList de usernames
     */
    @Override
    public ArrayList<Utilizador> getAllUsers() throws NullObjectException {
        ArrayList<Utilizador> users = new ArrayList<>();
        byte[] buffer = Protocol.getUsers();
        HashMap<String, String> map;
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));
        if( type == Type.SUCCESS) {
            int user_count = Integer.parseInt(map.get("user_count"));
            for(int i = 0; i < user_count; i++) {
                // TODO: account id
                String username = map.get(String.format("u_%d", i));
                String account_id = map.get(String.format("u_%d_id", i));
                boolean isEditor = Boolean.valueOf(map.get("u_%d_e"));
                Utilizador utilizador = new Utilizador(username, isEditor, false);
                utilizador.setAccount_id(account_id);
                users.add(utilizador);
            }
            return users;
        }
        else{
            throw new NullObjectException("No users on the server.");
        }
    }

    /**
     * Método para receber uma resposta do MULTICASTSERVER.
     * Ao enviar um pedido ao servidor Multicast, o servidor RMI aguarda por packet de resposta.
     * No caso do packet recebido ter a mesma hash do packet enviado, e um tipo de pacote diferente (garantindo
     * que não foi o pacote enviado) este assume que esse pacote é a resposta ao pedido efetuado. Verifica os
     * pacotes recebidos até obter uma resposta desejada.
     *
     * @param buffer buffer a enviar
     * @return hashmap com os valores da resposta
     */
    private HashMap<String, String> handleSendReply(byte[] buffer) {
        long sentTime = System.currentTimeMillis();
        String payload;
        boolean replied = false;
        HashMap<String, String> map = null;
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);
        String hash, hashSent = Protocol.getHash(buffer);
        Type type, typeSent = Protocol.getType(buffer);
        int i = 0;

        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!replied) {
            try {
                if (System.currentTimeMillis() - sentTime > SOCKET_TIMEOUT) {
                    socket.send(packet);
                    sentTime = System.currentTimeMillis();
                }
                do {
                    byte[] recvBuffer = new byte[MulticastServer.MAX_BUFFER_SIZE];
                    DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
                    socket.setSoTimeout(SOCKET_TIMEOUT);
                    socket.receive(recvPacket);

                    payload = new String(recvPacket.getData(), 0, recvPacket.getLength());
                    map = Protocol.getHashMap(payload);
                    hash = map.get("hash");
                    type = Type.valueOf(map.get("type"));

                    if (hash.equals(hashSent)) {
                        if (type != typeSent) {
                            replied = true;
                        } else {
                            i++;
                        }
                    }
                } while (i < 1);
            } catch (IOException e) {
                System.out.println("Multicast server nao respondem em " + SOCKET_TIMEOUT + "ms");
                replied = false;
            }
        }

        return map;
    }

    /**
     * Método de verificação do estado do servidor.
     *
     * @return true
     */
    @Override
    public boolean isAlive() {
        return true;
    }

    /**
     * Método de procura de um artista. Ao receber o nome por parâmetro, o servidor envia um pedido
     * de searchArtistRequest ao servidor Multicast, de onde recebe no caso de sucesso o nome do artista,
     * uma lista dos seus albúns e toda a informação relativa aos mesmos através do HashMap de resposta,
     * de forma a recriar os objectos para o cliente que efectuou o pedido.
     *
     * @param nome nome do artista
     * @return objecto artistas
     * @throws NullObjectException Excepção no caso não existir o objecto procurado.
     */
    @Override
    public Artista searchArtista(String nome) throws NullObjectException {
        Type type;
        Artista artista = null;
        HashMap<String, String> map;
        byte[] buffer = Protocol.searchArtistRequest(nome);

        map = handleSendReply(buffer);

        if (map != null) {
            type = Type.valueOf(map.get("type"));
            if (type == Type.SUCCESS) {
                artista = new Artista(map.get("name"));
                int item_count = Integer.parseInt(map.get("item_count"));
                for (int i = 0; i < item_count; i++) {
                    String titulo = map.get(String.format("album_%d_titulo", i));
                    String generoMusical = map.get(String.format("album_%d_genero", i));
                    Album album = new Album(titulo, artista, generoMusical);
                    artista.addAlbum(album);
                }

            } else if (type == Type.ERROR) {
                throw new NullObjectException(map.get("msg"));
            }
        }

        return artista;
    }

    /**
     * Método para a listagem de todos os artistas na base de dados.
     * Um pedido listArtist é feito ao servidor Multicast ao qual ele devolve um HashMap
     * com a informação de todos os artistas e o nome dos seus albúns, de forma a reconstruir
     * os Artista's e
     *
     * @return artistas ArrayList com a informação de todos os artistas na base de dados.
     * @throws NullObjectException nullobject
     */

    @Override
    public ArrayList<Artista> listArtist() throws NullObjectException {
        HashMap<String, String> map;
        byte[] buffer = Protocol.listArtist();
        ArrayList<Artista> artistas = new ArrayList<>();
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));

        if (type == Type.SUCCESS) {
            int artist_count = Integer.parseInt(map.get("a_count"));
            for (int i = 0; i < artist_count; i++) {
                String nomeArtista = map.get(String.format("a_%d", i));
                Artista artista = new Artista(nomeArtista);
                ArrayList<Album> albums = new ArrayList<>();
                int album_count = Integer.parseInt(map.get(String.format("a_%d_count", i)));
                for (int j = 0; j < album_count; j++) {
                    String tituloAlbum = map.get(String.format("a_%d_b_%d", i, j));
                    String generoMusical = map.get(String.format("a_%d_b_%d_genero", i, j));
                    String descricao = map.get(String.format("a_%d_b_%d_descricao", i, j));

                    Album album = new Album(tituloAlbum, artista);
                    album.setGeneroMusical(generoMusical);
                    album.setAlbumDesc(descricao);

                    // ler criticas
                    int criticas_num = Integer.parseInt(map.get(String.format("a_%d_b_%d_critica_count", i, j)));

                    for (int k = 0; k < criticas_num; k++) {
                        Critica critica;
                        try {
                            int pontuacao = Integer.parseInt(map.get(String.format("a_%d_b_%d_c_%d_pontuacao", i, j, k)));
                            String justificacao = map.get(String.format("a_%d_b_%d_c_%d_justificacao", i, j, k));
                            critica = new Critica(pontuacao, justificacao);
                            album.addCritica(critica);
                        } catch (ExceedsMaxLengthException e) {
                            e.printStackTrace();
                        }
                    }

                    // ler musicas
                    int musicas_num = Integer.parseInt(map.get(String.format("a_%d_b_%d_musica_count", i, j)));

                    for (int k = 0; k < musicas_num; k++) {
                        String titulo = map.get(String.format("a_%d_b_%d_m_%d_titulo", i, j, k));
                        String id = map.get(String.format("a_%d_b_%d_m_%d_id", i, j, k));
                        Musica musica = new Musica(titulo);
                        musica.setFilePath(id);
                        album.addMusica(musica);
                    }

                    // ler editores anteriores
                    int editors_count = Integer.parseInt(map.get(String.format("a_%d_b_%d_e_count", i, j)));

                    for (int k = 0; k < editors_count; k++) {
                        String username = map.get(String.format("a_%d_b_%d_e_%d_username", i, j, k));
                        album.getUsers().add(username);
                    }

                    albums.add(album);
                }
                artistas.add(artista);
                artista.setAlbuns(albums);

            }
        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));
        }

        return artistas;
    }

    /**
     * Método para a pesquisa dos detalhes de um album.
     * É feito um pedido getAlbumDetails ao servidor multicast, ao que ele responde com
     * HashMap com toda a informaçao relativa ao artista, músicas e críticas do album pedido,
     * recriando os objectos necessários de forma a devolver ao cliente o objecto Album pedido.
     *
     * @param album String de albúm a procurar
     * @return aux_album Objecto album encontrado
     * @throws NullObjectException nullobject
     * @throws ExceedsMaxLengthException justificação de critica superior a 300 caracteres
     */
    @Override
    public Album getAlbumDetails(String album) throws NullObjectException, ExceedsMaxLengthException {
        HashMap<String, String> map;
        byte[] buffer = Protocol.getAlbumDetails(album);
        Album aux_album = null;

        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));
        if (type == Type.SUCCESS) {
            int music_count = Integer.parseInt(map.get("music_count"));
            int critic_count = Integer.parseInt(map.get("critic_count"));
            String titulo = map.get("title");
            String autor = map.get("artist");
            String albumDesc = map.get("album_desc");
            Artista aux_artist = new Artista(autor);
            Musica aux_song;
            Critica aux_critica;
            aux_album = new Album(titulo, aux_artist);
            for (int i = 0; i < music_count; i++) {
                aux_song = new Musica(map.get(String.format("music_%d", i)));
                aux_album.addMusica(aux_song);
            }

            for (int i = 0; i < critic_count; i++) {
                int critica_pont = Integer.parseInt(map.get(String.format("critica_%d_pont", i)));
                String critica_just = map.get(String.format("critica_%d_just", i));

                try {

                    aux_critica = new Critica(critica_pont, critica_just);

                } catch (ExceedsMaxLengthException e) {

                    throw new ExceedsMaxLengthException("Critique exceeds 300 chars");

                }
                aux_album.addCritica(aux_critica);
            }
        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));

        }

        return aux_album;
    }

    /**
     * Método para a procura de um albúm.
     * É feito um pedido searchAlbumRequest ao servidor Multicast ao qual este devolve um HashMap com
     * toda a informação relativa ao objecto Album de nome "nome", sendo usada essa informação de forma
     * a recriar o objecto Album para devolver ao cliente que efectuou o pedido.
     *
     * @param nome nome do album
     * @return Album aux_album
     * @throws NullObjectException Album não encontrado na base de dados.
     */

    @Override
    public Album searchAlbum(String nome) throws NullObjectException, ExceedsMaxLengthException {
        HashMap<String, String> map;
        byte[] buffer = Protocol.searchAlbumRequest(nome);
        Album aux_album = null;
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));

        if (type == Type.SUCCESS) {
            Artista aux_artista = new Artista(map.get("artista"));
            aux_album = new Album(map.get("album"), aux_artista, map.get("genero"));
            String desc = map.get("desc");
            int music_count = Integer.parseInt(map.get("music_count"));
            int critic_count = Integer.parseInt(map.get("critic_count"));
            for (int i = 0; i < music_count; i++) {
                Musica aux_music = new Musica(map.get(String.format("music_%d", i)));
                aux_album.addMusica(aux_music);
            }
            for (int i = 0; i < critic_count; i++) {
                int pont = Integer.parseInt(map.get(String.format("critic_%d_pont", i)));
                String just = map.get(String.format("critic_%d_just", i));
                Critica aux_critica = new Critica(pont, just);
                aux_album.addCritica(aux_critica);
            }

        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));
        }

        return aux_album;
    }

    /**
     * Método para a criação de uma critica a um album. Ao receber o album a criticar, a pontuação e
     * justificação da critica, o servidor efectua um pedido createCritique ao servidor Multicast com estes mesmo parâmetros,
     * que, no caso de sucesso este devolve as informções relativas à critica criada de forma a recriar o objecto e devolve-lo
     * cliente.
     *
     * @param titulo    String nome do album a criticar.
     * @param pontuacao Pontuação da critica
     * @param critica   Justificação da critica
     * @return Critica aux_critica
     * @throws NullObjectException Album a criticar não se encontra na base de dados.
     */
    @Override
    public Critica createCritique(String titulo, String pontuacao, String critica) throws NullObjectException, ExceedsMaxLengthException {
        HashMap<String, String> map;
        byte[] buffer = Protocol.createCritique(titulo, pontuacao, critica);
        Critica aux_critica = null;
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));

        if (type == Type.SUCCESS) {
            int pont = Integer.parseInt(map.get("pont"));
            String just = map.get("just");
            aux_critica = new Critica(pont, just);
        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));
        }

        return aux_critica;
    }

    /**
     * Método para a procura de uma música.
     * Dado um nome de uma música recebido como parâmetro, o servidor RMI efectua um pedido searchMusicRequest
     * com esse mesmo nome, recebendo como resposta no caso de sucesso o titulo da música encontrada, de forma
     * a recriar o objecto Musica para devolver ao Cliente RMI.
     *
     * @param nome String nome da música a procurar
     * @return Musica musica
     * @throws NullObjectException Musica não encontrada na base de dados.
     */
    @Override
    public Musica searchMusic(String nome) throws NullObjectException {
        HashMap<String, String> map;
        byte[] buffer = Protocol.searchMusicaRequest(nome);
        Musica musica = null;
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));
        if (type == Type.SUCCESS) {
            String title = map.get("title");
            musica = new Musica(title);
        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));
        }

        return musica;
    }

    /**
     * Metodo de edição do nome e genero de uma musica.
     *
     * @param old_title nome do album
     * @param new_title novo nome do album
     * @param genero    novo genero do album
     * @return mensagem de erro/sucesso
     * @throws NullObjectException nullobject
     */
    @Override
    public String updateAlbum(String old_title, String new_title, String genero) throws NullObjectException {
        HashMap<String, String> map;
        String message = null;
        byte buffer[] = Protocol.updateAlbum(old_title, new_title, genero);
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));
        if (type == Type.SUCCESS) {
            message = map.get("msg");
        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));
        }
        return message;

    }

    /**
     * Método para o upload de musicas.
     * Ao efectuar um pedido uploadSong com o nome da música a criar e o nome do utilizador em questão, o servidor
     * Multicast devolve um HashMap com o address e port do servidor TCP ao qual se deve ligar para proceder ao upload.
     *
     * @param songName String nome da musica a fazer upload
     * @param username Nome do cliente que pretende fazer upload
     * @return HashMap com endereço e porto do TCP Server
     */
    @Override
    public HashMap<String, String> uploadSong(String songName, String username) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.uploadSong(songName, username);
        map = handleSendReply(buffer);
        map.remove("hash");
        return map;
    }

    /**
     * Método para o upload de musicas.
     * Ao efectuar um pedido downloadSong com o nome da música a criar e o nome do utilizador em questão, o servidor
     * @return HashMap com endereço e porto do TCP Server
     * Multicast devolve um HashMap com o address e port do servidor TCP ao qual se deve ligar para proceder ao download.
     *
     * @param songName String nome da musica a fazer download
     * @param username Nome do cliente que pretende fazer download
     */

    @Override
    public HashMap<String, String> downloadSong(String songName, String username) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.downloadSong(songName, username);
        map = handleSendReply(buffer);
        map.remove("hash");
        return map;
    }

    /**
     * Método para listar todos as músicas possíveis para download.
     * Dado o username do utilizador, é feito um pedido getDownloadableSongs ao servidor Multicast
     * ao qual este devolve um HashMap com todos as músicas disponíveis. É devolvido um ArrayList com
     * todas essas músicas ao cliente.
     *
     * @param username String username do cliente que efectuou o pedido
     * @return ArrayList musicas disponiveis
     */
    @Override
    public ArrayList<Musica> getDownloadableSongs(String username) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.getDownloadableSongs(username);

        ArrayList<Musica> musicas = new ArrayList<>();
        map = handleSendReply(buffer);
        int music_count = Integer.parseInt(map.get("music_count"));
        for (int i = 0; i < music_count; i++) {
            musicas.add(new Musica(map.get(String.format("music_%d", i))));
        }

        return musicas;
    }

    /**
     * Método para criar uma musica associada a um album
     *
     * @param songName  nome do album
     * @param albumName nome da musica
     * @return musica criada
     * @throws NullObjectException excepcao se a musica nao for criada
     */
    @Override
    public Musica createSong(String songName, String albumName) throws NullObjectException {
        HashMap<String, String> map;
        byte[] buffer = Protocol.createSong(songName, albumName);
        String song;
        Musica musica = null;
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));

        if (type == Type.SUCCESS) {
            song = map.get("music");
            musica = new Musica(song);
        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));
        }

        return musica;
    }

    @Override
    public boolean setSongId(String songName, String albumName, String artistName, String songId) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.setSongId(songName, albumName, artistName, songId);
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));

        return type == Type.SUCCESS;
    }

    /**
     * Método para a criação de um artista.
     * Dado um nome passado pelo cliente, é feito um pedido createArtist ao Multicast
     * que devolve, no caso de sucesso um Objecto Artista para o cliente.
     *
     * @param nome String nome do artista a criar
     * @return Artista artista
     * @throws NullObjectException Artista a criar já se encontra na base de dados.
     */
    @Override
    public Artista createArtist(String nome) throws NullObjectException {
        HashMap<String, String> map;
        String name;
        Artista artista = null;
        byte[] buffer = Protocol.createArtist(nome);

        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));

        if (type == Type.SUCCESS) {
            name = map.get("artist");
            artista = new Artista(name);

        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));
        }

        return artista;
    }


    /**
     * Método para a criação de um album.
     * O RMI Server, recebendo o titulo, artista e genero musical do album a criar, efectua um pedido
     * createAlbum com os parâmetros recebidos, retornando no caso de sucesso um HashMap com toda a informação
     * relativa ao album criado de forma retorna um Objecto Album para o cliente.
     *
     * @param titulo  String titulo do album a criar.
     * @param artista String autor do album.
     * @param genero  String genero musica do album
     * @return Album album criado
     * @throws NullObjectException Artista autor do album não encontrado
     */
    @Override
    public Album createAlbum(String titulo, String artista, String genero) throws NullObjectException {
        HashMap<String, String> map;
        String album_name, artist, gen;
        Album album = null;
        byte[] buffer = Protocol.createAlbum(titulo, artista, genero);

        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));

        if (type == Type.SUCCESS) {
            artist = map.get("artist");
            album_name = map.get("title");
            gen = map.get("genre");
            Artista aux_artista = new Artista(artist);
            album = new Album(album_name, aux_artista, gen);
            System.out.println(album.getTitulo());

        } else if (type == Type.ERROR) {
            throw new NullObjectException(map.get("msg"));
        }

        return album;
    }

    /**
     * Método de remoção de artistas.
     * É efectuado um pedido ao servidor Multicast com o nome do artista a remover.
     *
     * @param nome String nome do artista a remover
     * @return message mensagem devolvida pelo Multicast
     */
    @Override
    public boolean removeArtist(String nome) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.removeArtist(nome);
        map = handleSendReply(buffer);
        if(map!=null){
            Type type = Type.valueOf(map.get("type"));
            if(type==Type.SUCCESS){
                System.out.println(map.get("msg"));
                return true;

            }else if (type == Type.ERROR) {
                System.out.println(map.get("msg"));
                return false;
            }
        }
        return false;
    }

    /**
     * Método de remoção de albuns.
     * É efectuado um pedido ao servidor Multicast com o nome do album a remover.
     *
     * @param nome String nome do album a remover
     * @return message mensagem devolvida pelo Multicast
     */
    @Override
    public boolean removeAlbum(String nome) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.removeAlbum(nome);
        map = handleSendReply(buffer);
        if(map!=null){
            Type type = Type.valueOf(map.get("type"));
            if(type==Type.SUCCESS){
                System.out.println(map.get("msg"));
                return true;

            }else if (type == Type.ERROR) {
                System.out.println(map.get("msg"));
                return false;
            }
        }
        return false;
    }

    /**
     * Método de remoção de musicas.
     * É efectuado um pedido ao servidor Multicast com o nome da musica a remover.
     *
     * @param nome String nome do musica a remover
     * @return message mensagem devolvida pelo Multicast
     */
    @Override
    public boolean removeSong(String nome) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.removeSong(nome);
        System.out.println("REMOVING "+ nome);
        map = handleSendReply(buffer);
        if(map!=null){
            Type type = Type.valueOf(map.get("type"));
            if(type==Type.SUCCESS){
                System.out.println(map.get("msg"));
                return true;

            }else if (type == Type.ERROR) {
                System.out.println(map.get("msg"));
                return false;
            }
        }
        return true;
    }

    /**
     * Método de registo no servidor .
     * Este método recebe como parâmetros uma instância do cliente que se quer registar, o seu username e password.
     * É feito um pedido registerRequest ao servidor que, no caso de sucesso e registo na base de dados, devolve as
     * permissões do utilizador assim como o seu username de forma a construir um objecto do tipo cliente;
     * @param callback referencia do cliente
     * @param nome username
     * @param password password
     * @return Utilizador utilizador registado na base de dados.
     */
    @Override
    public Utilizador register(RMIClientInterface callback, String nome, String password) throws RemoteException {
        Utilizador utilizador = null;
        HashMap<String, String> map;
        byte[] buffer = Protocol.registerRequest(nome, password);
        Type type;

        map = handleSendReply(buffer);

        if (map != null) {
            type = Type.valueOf(map.get("type"));
            if (type == Type.LOGIN_SUCCESS) {
                boolean isEditor = Boolean.valueOf(map.get("isEditor"));
                boolean isAdmin = Boolean.valueOf(map.get("isAdmin"));
                String username = map.get("username");
                utilizador = new Utilizador(username, isEditor, isAdmin);
                String access_token = map.get("access_token");
                utilizador.setAccess_token(access_token);
            } else {
                callback.print(map.get("msg"));
            }
        }
        return utilizador;
    }

    /**
     * Método de login no servidor.
     * Recebendo um account_id efectua um pedido de login.
     *
     * @param account_id login com a accont_id do dropbox
     * @param access_token access_token
     * @return Utilizador utilizador loggado na base de dados.
     */
    @Override
    public Utilizador register(String account_id, String username, String access_token) throws RemoteException {
        Utilizador utilizador = null;
        HashMap<String, String> map;
        byte[] buffer = Protocol.dropboxRegister(account_id, username, access_token);
        Type type;

        map = handleSendReply(buffer);

        if (map != null) {
            type = Type.valueOf(map.get("type"));
            if (type == Type.LOGIN_SUCCESS) {
                boolean isEditor = Boolean.valueOf(map.get("isEditor"));
                boolean isAdmin = Boolean.valueOf(map.get("isAdmin"));
                utilizador = new Utilizador(username, isEditor, isAdmin);
                utilizador.setAccess_token(access_token);
                utilizador.setAccount_id(account_id);
            }
        }
        return utilizador;
    }

    /**
     * Método de login no servidor.
     * Recebendo uma instância do cliente, o seu username e password, o servidor RMI efectua um pedido
     * de loginRequest ao Multicast com os parâmetros username e password. No caso de sucesso o Multicast
     * devolve ao RMIServer os atributos do Utilizador Cliente, de forma a criar e devolver o objeto Utilizador
     * @param callback Instância do cliente que está a tentar fazer login.
     * @param nome Username do utilizador
     * @param password Password do utilizador
     * @return Utilizador utilizador loggado na base de dados.
     */
    @Override
    public Utilizador login(RMIClientInterface callback, String nome, String password) throws RemoteException {
        Utilizador utilizador = null;
        HashMap<String, String> map;
        byte[] buffer = Protocol.loginRequest(nome, password);
        Type type;

        map = handleSendReply(buffer);

        if (map != null) {
            type = Type.valueOf(map.get("type"));
            if (type == Type.LOGIN_SUCCESS) {
                boolean isEditor = Boolean.valueOf(map.get("isEditor"));
                boolean isAdmin = Boolean.valueOf(map.get("isAdmin"));
                String username = map.get("username");
                utilizador = new Utilizador(username, isEditor, isAdmin);
                String access_token = map.get("access_token");
                String account_id = map.get("account_id");
                utilizador.setAccount_id(account_id);
                utilizador.setAccess_token(access_token);
            } else {
                callback.print(map.get("msg"));
            }
        }
        return utilizador;
    }

    /**
     * Método de login no servidor.
     * Recebendo um account_id efectua um pedido de login.
     *
     * @param account_id login com a accont_id do dropbox
     * @return Utilizador utilizador loggado na base de dados.
     */
    @Override
    public Utilizador login(String account_id) throws RemoteException {
        Utilizador utilizador = null;
        HashMap<String, String> map;
        byte[] buffer = Protocol.loginRequest(account_id);
        Type type;

        map = handleSendReply(buffer);

        if (map != null) {
            type = Type.valueOf(map.get("type"));
            if (type == Type.LOGIN_SUCCESS) {
                boolean isEditor = Boolean.valueOf(map.get("isEditor"));
                boolean isAdmin = Boolean.valueOf(map.get("isAdmin"));
                String username = map.get("username");
                String id = map.get("account_id");
                utilizador = new Utilizador(username, isEditor, isAdmin);
                String access_token = map.get("access_token");
                utilizador.setAccess_token(access_token);
                utilizador.setAccount_id(account_id);
            }
        }
        return utilizador;
    }

    /**
     * Método para atualizar o account id de um utilizador.
     *
     * @param username   Nome do utilizador
     * @param account_id account_id.
     * @return true se for atualizado
     */
    @Override
    public boolean setAccountId(String username, String account_id, String access_token) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.setAccountId(username, account_id, access_token);
        Type type;

        map = handleSendReply(buffer);

        if (map != null) {
            type = Type.valueOf(map.get("type"));
            return type == Type.LOGIN_SUCCESS;
        }
        return false;
    }

    /**
    * Método para o pedido de notificações.
    * Através do username recebido por parâmetro, o servido RMI efectua um pedido getNotifications
    * pedindo ao servidor Multicast todas as notificações relativas ao Cliente de nome "username".
    *@param username Nome do utilizador a verificar
    *@return notifications ArrayList de todas as notificações.
    */

    @Override
    public ArrayList<String> getNotifications(String username) {
        HashMap<String, String> map;
        ArrayList<String> notifications = new ArrayList<>();
        byte[] buffer = Protocol.getNotifications(username);
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));
        if (type == Type.SUCCESS) {
            int not_count = Integer.parseInt(map.get("not_count"));
            for (int i = 0; i < not_count; i++) {
                notifications.add(map.get(String.format("not_%d", i)));
            }
        }

        return notifications;
    }

    /**
    * Método para a alteração da descrição de um album.
    * Através do parâmetros recebidos, é feito um pedido editAlbumDesc ao servidor Multicast que, no caso de sucesso
    * irá devolver ao servidor RMI um HashMap com todos os editores desse album, de forma a notificá--los das alterações
    * feitas.
    *@param album String nome do album
    *@param msg String descrição do album
    *@param username String username
    *@return Mensagem devolvida pelo Multicast server
    */

    @Override
    public String editAlbumDesc(String album, String msg, String username) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.editAlbumDesc(album, msg, username);
        int n_editors;
        String album_name, message;
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));
        album_name = map.get("album");
        if (type == Type.SUCCESS) {
            n_editors = Integer.parseInt(map.get("n_editors"));
            message = "Descrição alterada";
            for (int i = 0; i < n_editors; i++) {
                notifyClient(map.get(String.format("editor_%d", i)), album_name + " foi alterado.");
            }
        } else {
            message = "Erro.";
        }
        return message;
    }

    @Override
    public String editSong(String oldName, String newName) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.editSong(oldName, newName);
        map = handleSendReply(buffer);
        return map.get("msg");
    }

    /**
    * Método para a atribuição de atributos de editor.
    * É efectuado um pedido makeEditor ao servidor Multicast com o username do utilizaodr que
    * se pretende alterar.
    *@param username String username
    *@return message String mensagem retornada pelo Multicast
    */
    @Override
    public String makeEditor(String username) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.makeEditor(username);
        String message;
        map = handleSendReply(buffer);
        Type type = Type.valueOf(map.get("type"));
        message = map.get("msg");
        if (type == Type.SUCCESS) {
            RMIClientInterface client = clients.get(username);
            try {
                client.givePermission();


            } catch (RemoteException | NullPointerException e) {
                System.out.println("user is offline");
            }
            notifyClient(username, "You now have editor permissions.");

        }
        else{
            message = "ERROR|User not found";
        }
        return message;
    }

    /**
    * Método para a partilha de uma música com outro utilizador.
    * É efectuado um pedido shareSong ao servidor Multicast, retornando a mensagem de volta ao cliente.
    *@param musica String nome da musica a partilhar
    *@param username String
    *@return message String resposta do servidor Multicast
    */
    @Override
    public String shareSong(String musica, String username) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.shareSong(musica, username);
        map = handleSendReply(buffer);
        return map.get("msg");
    }

    /**
    * Método para a notificação de clientes.
    * Ao receber um username de um cliente a notificar e a respectiva mensagem, verifica se este faz parte do
    * ArrayList de Clientes, caso não faça parte este será notificado posteriormente quando efectuar login.
    *@param username String username a notificar
    *@param message String mensagem da notificação
    */
    private void notifyClient(String username, String message) {
        RMIClientInterface client = clients.get(username);
        boolean online = true;
        if (client != null) {
            try {
                client.print(message);
            } catch (RemoteException e) {
                System.out.println("utilizador está offline - notificando...");
                online = false;
            }
        } else {
            System.out.println("utilizador está offline");
            online = false;
        }
        if (!online) {
            notifyOffline(username, message);
        }
    }
    /**
    * Método para a notificação de um cliente offline.
    * É efectuado um pedido offlineNotifications ao servidor Multicast, com o username a notificar e respectiva mensagem.
    *@param username String nome do utilizador a notificar
    *@param msg String mensagem da notificação
    */
    private void notifyOffline(String username, String msg) {
        HashMap<String, String> map;
        byte[] buffer = Protocol.offlineNotifications(username, msg);
        map = handleSendReply(buffer);
        System.out.println(map);
    }
}
