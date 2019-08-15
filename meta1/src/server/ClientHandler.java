package server;

import models.*;
import protocol.Protocol;
import protocol.Type;
import storage.FicheiroDeObjectos;
import utils.Control;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe para lidar com um pedido feito por um servidor RMI.
 */
class ClientHandler extends Thread {
    private long SLEEP_TIME = 1;
    private CopyOnWriteArrayList<Utilizador> utilizadores;
    private CopyOnWriteArrayList<Artista> artistas;
    private HashMap<String, String> map;
    private Control control;
    private HashMap<String, ArrayList> offlineNotifications;

    /**
     * Construtor
     *
     * @param packet               pacote do pedido
     * @param utilizadores         array de utilizadores
     * @param artistas             array de artistas guardados
     * @param offlineNotifications array de notificações por entregar a utilizadores
     */
    ClientHandler(DatagramPacket packet, CopyOnWriteArrayList<Utilizador> utilizadores, CopyOnWriteArrayList<Artista> artistas, HashMap<String, ArrayList> offlineNotifications) {
        String payload = new String(packet.getData(), 0, packet.getLength());
        this.map = Protocol.getHashMap(payload);
        this.utilizadores = utilizadores;
        this.artistas = artistas;
        this.control = new Control();
        this.offlineNotifications = offlineNotifications;
    }

    /**
     * Método que é executado numa thread própia, simples switch case com base no tipo
     * da mensagem recebida. Existe um método diferente para cada tipo de pedido.
     */
    @Override
    public void run() {
        Type type;

        try {
            type = Type.valueOf(map.get("type"));
        } catch (IllegalArgumentException e) {
            type = Type.INVALID;
        }

        switch (type) {
            case LOGIN:
                userLogin();
                break;
            case LOGIN_ACCOUNT_ID:
                userLoginAccountId();
                break;
            case DROPBOX_REGISTER:
                userDropboxRegister();
                break;
            case SET_ACCOUNT_ID:
                setAccountId();
                break;
            case SET_SONG_ID:
                setSongId();
                break;
            case REGISTER:
                userRegister();
                break;
            case ADD_ARTIST:
                addArtist();
                break;
            case EDIT_ARTIST:
                editArtist();
                break;
            case SEARCH_ARTIST:
                searchArtist();
                break;
            case SEARCH_MUSIC:
                searchMusic();
                break;
            case SEARCH_ALBUM:
                searchAlbum();
                break;
            case CREATE_ARTIST:
                createArtist();
                break;
            case CREATE_ALBUM:
                createAlbum();
                break;
            case REMOVE_ARTIST:
                removeArtist();
                break;
            case ALBUM_DETAILS:
                getAlbumDetails();
                break;
            case CREATE_CRITIQUE:
                createCritique();
                break;
            case CREATE_SONG:
                createSong();
                break;
            case MAKE_EDITOR:
                makeEditor();
                break;
            case REMOVE_ALBUM:
                removeAlbum();
                break;
            case REMOVE_SONG:
                removeSong();
                break;
            case LIST_ARTIST:
                listArtist();
                break;
            case GET_NOTS:
                getNotifications();
                break;
            case OFFLINE_NOTS:
                offlineNotify();
                break;
            case UPLOAD_MUSIC:
                uploadSong();
                break;
            case DOWNLOAD_MUSIC:
                downloadSong();
                break;
            case UPDATE_ALBUM:
                updateAlbum();
                break;
            case EDIT_ALBUM_DESC:
                editAlbumDesc();
                break;
            case GET_DOWNLOADABLE_SONGS:
                getDownloadableSongs();
                break;
            case SHARE_SONG:
                shareSong();
                break;
            case EDIT_SONG:
                editSong();
                break;
            case ALL_USERS:
                getAllUsers();
                break;
            case SUCCESS:
            case ERROR:
            case LOGIN_SUCCESS:
                break;
            default:
                System.out.println("INVALID");
                break;
        }

        saveIntoStorage();
    }

    /**
     * Função auxiliar para procurar um album na lista de artistas.
     *
     * @param nome nome do album a procurar
     * @return referencia para o album se ele existir, null caso contrário.
     */
    private Album getAlbum(String nome) {
        for (Artista artista : artistas) {
            for (Album album : artista.getAlbuns()) {
                if (album.equals(nome)) {
                    return album;
                }
            }
        }
        return null;
    }

    /**
     * Função auxiliar para procurar um album na lista de artistas.
     *
     * @param autor  nome do artista
     * @param titulo nome do album a procurar
     * @return referencia para o album se ele existir, null caso contrário.
     */
    private Album getAlbum(String titulo, String autor) {
        for (Artista artista : artistas) {
            if (artista.equals(autor)) {
                for (Album album : artista.getAlbuns()) {
                    if (album.equals(titulo)) {
                        return album;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Função auxiliar para procurar uma musica na lista de artistas.
     *
     * @param titulo nme da musica
     * @param autor  nome do artista
     * @param album  nome do album a procurar
     * @return referencia para o album se ele existir, null caso contrário.
     */
    private Musica getMusica(String titulo, String album, String autor) {
        for (Artista artista : artistas) {
            if (artista.equals(autor)) {
                for (Album aux : artista.getAlbuns()) {
                    if (aux.equals(album)) {
                        for (Musica musica : aux.getMusicas()) {
                            if (musica.equals(titulo)) {
                                return musica;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Funçao auxiliar para procurar um artista na lista de artistas.
     *
     * @param nome nome do artista a procurar
     * @return referencia para o artista se existir, null caso contrário.
     */
    private Artista getArtist(String nome) {
        for (Artista artista : artistas) {
            if (artista.equals(nome)) {
                return artista;
            }
        }
        return null;

    }

    /**
     * Função auxiliar para gerir o envio da resposta ao pedido atual.
     * É criada uma instancia de {@link server.ReplyListener} para ficar à escuta durante
     * um tempo aleatório. Se este servidor receber uma resposta enviada por outro servidor, este nao
     * envia resposta. Caso nenhum outro servidor tenha enviado, este envia.
     *
     * @param buffer array de bytes da mensagem a ser enviada
     * @return true se a mensagem foi enviada, false se nao foi
     */
    private boolean sendReply(byte[] buffer) {
        MulticastSocket socket;

        long sleep = (long) (Math.random() * SLEEP_TIME);
        ReplyListener replyListener = new ReplyListener(buffer, control, (int) sleep);
        System.out.println("handler: sleeping for " + sleep);
        try {
            sleep(sleep);
            replyListener.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!control.flag) {
            try {
                socket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(MulticastServer.MULTICAST_ADDRESS);
                socket.joinGroup(group);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MulticastServer.MULTICAST_PORT);
                System.out.println("handler: sending reply...");
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("handler: not sending reply");
        }

        return !control.flag;
    }

    /**
     * Função para tratar um pedido de edição de nome de artista.
     */
    private void editArtist() {
        String oldName = map.get("oldName");
        String newName = map.get("newName");
        byte[] buffer = Protocol.error("Artist not found", map.get("hash"));

        for (Artista artista : artistas) {
            if (artista.equals(oldName)) {
                artista.setNome(newName);
                buffer = Protocol.success("Artist name changed", map.get("hash"));
            }
        }
        sendReply(buffer);
    }

    /**
     * Função para tratar um pedido de edição de nome de musica.
     */
    private void editSong() {
        String oldName = map.get("oldname");
        String newName = map.get("newname");
        byte[] buffer = Protocol.error("Musica nao encontrada.", map.get("hash"));

        for (Artista artista : artistas) {
            for (Album album : artista.getAlbuns()) {
                for (Musica musica : album.getMusicas()) {
                    if (musica.equals(oldName)) {
                        musica.setTitulo(newName);
                        buffer = Protocol.success("Nome da musica alterado.", map.get("hash"));
                    }
                }
            }
        }
        sendReply(buffer);
    }

    /**
     * Método auxiliar para listar utilizadores.
     */
    private void getAllUsers() {
        byte[] buffer;
        String hash = map.get("hash");

        HashMap<String, String> replyMap = new HashMap<>();

        int user_count = utilizadores.size();
        replyMap.put("user_count", String.valueOf(user_count));
        for(int i = 0; i < user_count; i++){
            replyMap.put(String.format("u_%d", i), utilizadores.get(i).getUsername());
            replyMap.put(String.format("u_%d_id", i), utilizadores.get(i).getAccount_id());
            replyMap.put(String.format("u_%d_e", i), String.valueOf(utilizadores.get(i).getIsEditor()));
        }

        buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        sendReply(buffer);
    }

    /**
     * Função auxiliar de procura de um utilizador.
     *
     * @param username nome do utilizador a procurar
     * @return referencia para o utilizador se exister, null caso contrario
     */
    private Utilizador getUser(String username) {
        for (Utilizador user : utilizadores) {
            if (user.equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Função auxiliar para procurar se um artista existe.
     *
     * @param nome nome do utilizador a procurar
     * @return true se existir, false se nao
     */
    private boolean artistExists(String nome) {
        for (Artista artista : artistas) {
            if (artista.equals(nome)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Função para atualizacao de nome e genero de um dado album.
     */
    private void updateAlbum() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String titulo = map.get("old_title");
        String new_title = map.get("new_title");
        String genero = map.get("genre");
        String hash = map.get("hash");
        Album aux = getAlbum(titulo);
        if (aux != null) {
            aux.setTitulo(new_title);
            aux.setGeneroMusical(genero);
            replyMap.put("msg", String.format("%s Editado com sucesso", aux.getTitulo()));
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);

        } else {
            replyMap.put("msg", String.format("Album %s not found.", titulo));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        }

        sendReply(buffer);
    }

    /**
     * Função de tratamento de pedido de dar permissao de editor a um utilizador.
     * É verificado se o utilizador existe, caso nao exista é devolvida uma mensagem de erro.
     * Se existir, é lhe dado permissao de editor e envia-se mensagem de sucesso ao servidor RMI.
     */
    private void makeEditor() {
        byte[] buffer;
        String username = map.get("username");
        String hash = map.get("hash");
        Utilizador user = getUser(username);
        if (user != null) {
            if (user.getIsEditor()) {
                buffer = Protocol.error("Utilizador já era editor.", hash);
            } else {
                user.setIsEditor(true);
                buffer = Protocol.success(String.format("'%s' é agora editor", username), hash);
            }
        } else {
            buffer = Protocol.error(String.format("Utilizador '%s' nao existe.", username), hash);
        }

        sendReply(buffer);
    }

    /**
     * Função de tratamento de pedido de adicção de uma avaliação a um album.
     * Em caso de erro (album nao existir, critica ser mais de 300 char) é enviada a mensagem
     * de erro correspondente
     */
    private void createCritique() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String album = map.get("title");
        String critique = map.get("msg");
        int pont = Integer.parseInt(map.get("points"));
        String hash = map.get("hash");
        Album aux = getAlbum(album);
        try {
            if (aux != null) {
                Critica critica = new Critica(pont, critique);
                if (aux.addCritica(critica)) {
                    replyMap.put("pont", String.valueOf(critica.getPontuacao()));
                    replyMap.put("just", critica.getJustificacao());
                    buffer = Protocol.sendHashMapSuccess(replyMap, hash);

                } else {
                    replyMap.put("msg", "Erro ao adicionar critica.");
                    buffer = Protocol.sendHashMapError(replyMap, hash);
                }
            } else {
                replyMap.put("msg", "Album not found");
                buffer = Protocol.sendHashMapError(replyMap, hash);
            }
        } catch (ExceedsMaxLengthException e) {
            replyMap.put("msg", "Critique exceeds 300 chars");
            buffer = Protocol.sendHashMapError(replyMap, hash);
        }

        sendReply(buffer);
    }

    /**
     * Metodo de tratamento de um pedido de criação de um artista novo.
     */
    private void createArtist() {
        HashMap<String, String> replyMap = new HashMap<>();
        byte[] buffer;
        String name = map.get("nome");
        String hash = map.get("hash");
        if (artistExists(name)) {
            replyMap.put("msg", String.format("Artista '%s' já existe", name));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        } else {
            Artista aux = new Artista(name);
            artistas.add(aux);
            replyMap.put("artist", name);
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);

        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de criação de uma música nova.
     */
    private void createSong() {
        byte[] buffer;
        String albumName = map.get("album");
        String songName = map.get("title");
        String hash = map.get("hash");
        HashMap<String, String> replyMap = new HashMap<>();
        Album album = getAlbum(albumName);
        Musica musica;
        if (album != null) {
            musica = album.getMusica(songName);
            if (musica != null) {
                replyMap.put("msg", String.format("A música '%s' já existe no album '%s'.", songName, albumName));
                buffer = Protocol.sendHashMapError(replyMap, hash);

            } else {
                musica = new Musica(songName);
                album.addMusica(musica);
                replyMap.put("music", songName);
                buffer = Protocol.sendHashMapSuccess(replyMap, hash);
            }
        } else {
            replyMap.put("msg", String.format("O albúm '%s' não existe.", albumName));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de criação de um album.
     */
    private void createAlbum() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String titulo = map.get("title");
        String artista = map.get("artist");
        String genero = map.get("genre");
        String hash = map.get("hash");

        Artista artist = getArtist(artista);
        if (artist == null) {
            replyMap.put("msg", String.format("Artista %s não encontrado.", artista));
            buffer = Protocol.sendHashMapError(replyMap, hash);
            sendReply(buffer);
            return;
        }

        Album aux_album = new Album(titulo, artist, genero);

        if (artist.addAlbum(aux_album)) {
            replyMap.put("title", aux_album.getTitulo());
            replyMap.put("artist", aux_album.getAutor().getNome());
            replyMap.put("genre", aux_album.getGeneroMusical());
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        } else {
            replyMap.put("msg", String.format("Erro ao adicionar o albúm %s.", titulo));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de atualizacao de song id de uma musica.
     */
    private void setSongId() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String titulo = map.get("songName");
        String artista = map.get("artistName");
        String album = map.get("albumName");
        String id = map.get("songId");
        String hash = map.get("hash");

        Musica musica = getMusica(titulo, album, artista);
        if (musica == null) {
            replyMap.put("msg", String.format("Musica %s não encontrado.", titulo));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        } else {
            musica.setFilePath(id);
            replyMap.put("msg", "Song id set");
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de procura de artista.
     */
    private void searchArtist() {
        boolean found = false;
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String name = map.get("nome");

        for (Artista aux : artistas) {
            if (aux.equals(name)) {
                found = true;
                ArrayList<Album> albuns = aux.getAlbuns();
                replyMap.put("name", aux.getNome());
                replyMap.put("item_count", String.valueOf(albuns.size()));
                if (albuns.size() > 0) {
                    for (int i = 0; i < albuns.size(); i++) {
                        replyMap.put(String.format("album_%d_titulo", i), albuns.get(i).getTitulo());
                        replyMap.put(String.format("album_%d_genero", i), albuns.get(i).getGeneroMusical());
                    }
                }
            }
        }

        if (!found) {
            replyMap.put("msg", String.format("O artista %s não existe.", name));
            buffer = Protocol.sendHashMapError(replyMap, map.get("hash"));
        } else {
            buffer = Protocol.sendHashMapSuccess(replyMap, map.get("hash"));
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de remoção de um artista.
     */
    private void removeArtist() {
        byte[] buffer;
        Artista artista;
        String name = map.get("nome");
        String hash = map.get("hash");

        artista = getArtist(name);

        if (artista != null) {
            if (artistas.remove(artista)) {
                buffer = Protocol.success(String.format("Artista '%s' removido.", name), hash);
            } else {
                buffer = Protocol.error(String.format("Erro ao remover o artista '%s'.", name), hash);
            }
        } else {
            buffer = Protocol.error(String.format("O artista '%s' não existe.", name), hash);
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de remoção de um album.
     */
    private void removeAlbum() {
        byte[] buffer;
        Album album;
        String name = map.get("nome");
        String hash = map.get("hash");

        album = getAlbum(name);

        if (album != null) {
            if (album.getAutor().getAlbuns().remove(album)) {
                buffer = Protocol.success(String.format("Album '%s' removido.", name), hash);
            } else {
                buffer = Protocol.error(String.format("Erro ao remover o album '%s'.", name), hash);
            }
        } else {
            buffer = Protocol.error(String.format("O album '%s' não existe.", name), hash);
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de remoção de uma música.
     */
    private void removeSong() {
        byte[] buffer;
        boolean found = false;
        Musica musica = null;
        Album album = null;
        String name = map.get("nome");
        String hash = map.get("hash");
        for (Artista artista : artistas) {
            for (Album aaux : artista.getAlbuns()) {
                for (Musica maux : aaux.getMusicas()) {
                    if (maux.equals(name)) {
                        found = true;
                        musica = maux;
                        album = aaux;
                        break;
                    }
                }
            }
        }

        if (!found) {
            buffer = Protocol.error(String.format("A música '%s' não existe.", name), hash);
        } else {
            if (album.removeMusica(musica)) {
                buffer = Protocol.success(String.format("Música '%s' removida do albúm '%s'.", name, album.getTitulo()), hash);
            } else {
                buffer = Protocol.error(String.format("Erro ao remover a música '%s' do albúm '%s'.", name, album.getTitulo()), hash);
            }
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de alteração da descrição de um album.
     */
    private void editAlbumDesc() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String username = map.get("username");
        String album = map.get("album");
        String desc = map.get("msg");
        String hash = map.get("hash");
        Album aux = getAlbum(album);
        if (aux != null) {
            aux.setAlbumDesc(desc, username);
            ArrayList<String> editors = aux.getUsers();
            int n_editors = 0;
            if (editors != null) {
                n_editors = editors.size();
            }
            replyMap.put("album", album);
            replyMap.put("n_editors", String.valueOf(n_editors));
            for (int i = 0; i < n_editors; i++) {
                replyMap.put(String.format("editor_%d", i), editors.get(i));
            }
            System.out.println("editalbumdesc");
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        } else {
            replyMap.put("msg", String.format("Album %s não existe", album));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        }
        sendReply(buffer);

    }

    /**
     * Método de tratamento de um pedido de leitura dos detalhes de um album.
     */
    private void getAlbumDetails() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String album = map.get("album");
        String hash = map.get("hash");
        Album aux = getAlbum(album);
        if (aux != null) {
            ArrayList<Musica> aux_musicas = aux.getMusicas();
            ArrayList<Critica> aux_criticas = aux.getCriticas();
            int music_count = aux_musicas.size();
            int critic_count = aux_criticas.size();
            String albumDesc = aux.getAlbumDesc();
            replyMap.put("title", aux.getTitulo());
            replyMap.put("artist", aux.getAutor().getNome());
            replyMap.put("music_count", String.valueOf(music_count));
            replyMap.put("critic_count", String.valueOf(critic_count));
            replyMap.put("album_desc", albumDesc);


            if (music_count > 0) {
                for (int i = 0; i < music_count; i++) {
                    replyMap.put(String.format("music_%d", i), aux_musicas.get(i).getTitulo());
                }
            }
            if (critic_count > 0) {
                for (int i = 0; i < critic_count; i++) {
                    replyMap.put(String.format("critica_%d_pont", i), String.valueOf(aux_criticas.get(i).getPontuacao()));
                    replyMap.put(String.format("critica_%d_just", i), String.valueOf(aux_criticas.get(i).getJustificacao()));
                }
            }

            buffer = Protocol.sendHashMapSuccess(replyMap, map.get("hash"));
        } else {
            replyMap.put("msg", String.format("Album %s não encontrado.", album));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        }
        sendReply(buffer);


    }

    /**
     * Método de tratamento de um pedido de leitura da lista de todos os artistas.
     */
    private void listArtist() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        ArrayList<Album> albuns;
        Artista aux_artist;
        Album aux_album;
        int artist_num = artistas.size();
        if (artist_num <= 0) {
            replyMap.put("msg", "Não existem artistas na base de dados");
            buffer = Protocol.sendHashMapError(replyMap, map.get("hash"));
        } else {
            replyMap.put("a_count", String.valueOf(artist_num));
            for (int i = 0; i < artist_num; i++) {
                aux_artist = artistas.get(i);
                albuns = aux_artist.getAlbuns();
                int album_num = albuns.size();
                replyMap.put(String.format("a_%d", i), aux_artist.getNome());
                replyMap.put(String.format("a_%d_count", i), String.valueOf(albuns.size()));
                if (album_num > 0) {
                    for (int j = 0; j < album_num; j++) {
                        aux_album = albuns.get(j);
                        replyMap.put(String.format("a_%d_b_%d", i, j), aux_album.getTitulo());
                        replyMap.put(String.format("a_%d_b_%d_genero", i, j), aux_album.getGeneroMusical());
                        replyMap.put(String.format("a_%d_b_%d_descricao", i, j), aux_album.getAlbumDesc());

                        // criticas
                        int criticas_num = aux_album.getCriticas().size();
                        replyMap.put(String.format("a_%d_b_%d_critica_count", i, j), String.valueOf(criticas_num));

                        for (int k = 0; k < criticas_num; k++) {
                            Critica critica = aux_album.getCriticas().get(k);
                            replyMap.put(String.format("a_%d_b_%d_c_%d_pontuacao", i, j, k), String.valueOf(critica.getPontuacao()));
                            replyMap.put(String.format("a_%d_b_%d_c_%d_justificacao", i, j, k), critica.getJustificacao());
                        }

                        // musicas
                        int musicas_count = aux_album.getMusicas().size();
                        replyMap.put(String.format("a_%d_b_%d_musica_count", i, j), String.valueOf(musicas_count));

                        for (int k = 0; k < musicas_count; k++) {
                            Musica musica = aux_album.getMusicas().get(k);
                            replyMap.put(String.format("a_%d_b_%d_m_%d_titulo", i, j, k), musica.getTitulo());
                            replyMap.put(String.format("a_%d_b_%d_m_%d_id", i, j, k), musica.getFilePath());
                        }

                        // editores antigos
                        int editors_count = aux_album.getUsers().size();
                        replyMap.put(String.format("a_%d_b_%d_e_count", i, j), String.valueOf(editors_count));

                        for (int k = 0; k < editors_count; k++) {
                            String username = aux_album.getUsers().get(k);
                            replyMap.put(String.format("a_%d_b_%d_e_%d_username", i, j, k), username);
                        }
                    }
                }
            }
            buffer = Protocol.sendHashMapSuccess(replyMap, map.get("hash"));
        }

        sendReply(buffer);


    }

    /**
     * Método de tratamento de um pedido de upload de uma música.
     * É verificada a existencia da musica.
     * Caso a música exista, é criado um servidor TCP que fica à espera da ligação do cliente
     * e é tambem enviado ao cliente o endereço e porto do servidor.
     */
    private void uploadSong() {
        byte[] buffer;
        Musica musica = null;
        TCPServer tcpServer = null;
        HashMap<String, String> replyMap = new HashMap<>();
        String hash = map.get("hash");
        String songName = map.get("music_title");
        String username = map.get("username");
        String fileName = null;

        for (Artista art : artistas) {
            for (Album alb : art.getAlbuns()) {
                for (Musica mus : alb.getMusicas()) {
                    if (mus.equals(songName)) {
                        musica = mus;
                        fileName = String.format("%s;%s;%s", art.getNome(), alb.getTitulo(), mus.getTitulo());
                    }
                }
            }
        }

        if (musica != null) {
            tcpServer = new TCPServer(musica, fileName);
            musica.addUser(username);
            musica.setFilePath(fileName + ".mp3");
            replyMap.put("address", String.valueOf(tcpServer.getAddress()));
            replyMap.put("port", String.valueOf(tcpServer.getPort()));
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        } else {
            buffer = Protocol.error("Musica nao existe.", hash);
        }

        if (sendReply(buffer) && musica != null) {
            tcpServer.start();
        } else if (musica != null) {
            tcpServer.shutdown();
        }
    }

    /**
     * Método de tratamento de um pedido de download de uma musica.
     * Verifica-se se a música existe, só é devolvida uma resposta caso este servidor tenha o ficheiro.
     */
    private void downloadSong() {
        byte[] buffer;
        Musica musica = null;
        boolean canDownload = false;
        TCPServer tcpServer = null;
        HashMap<String, String> replyMap = new HashMap<>();
        String hash = map.get("hash");
        String songName = map.get("music_title");
        String username = map.get("username");

        for (Artista art : artistas) {
            for (Album alb : art.getAlbuns()) {
                for (Musica mus : alb.getMusicas()) {
                    if (mus.equals(songName)) {
                        musica = mus;
                    }
                }
            }
        }

        if (musica != null && musica.getFilePath() != null && musica.getUsers().contains(username)) {
            canDownload = true;
            tcpServer = new TCPServer(musica.getFilePath());
            replyMap.put("address", String.valueOf(tcpServer.getAddress()));
            replyMap.put("port", String.valueOf(tcpServer.getPort()));
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        } else {
            buffer = Protocol.error("Musica nao existe.", hash);
        }

        if (canDownload) {
            // enviar sempre visto que só este servidor é que tem o ficheiro
            try {
                MulticastSocket socket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(MulticastServer.MULTICAST_ADDRESS);
                socket.joinGroup(group);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MulticastServer.MULTICAST_PORT);
                System.out.println("handler: sending reply...");
                socket.send(packet);
                tcpServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Funcao que responde ao cliente com uma listagem de todas as musicas que um dado utilizador
     * consegue fazer download a partir deste servidor.
     */
    private void getDownloadableSongs() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String hash = map.get("hash");
        String username = map.get("username");

        int music_count = 0;
        for (Artista art : artistas) {
            for (Album alb : art.getAlbuns()) {
                for (Musica mus : alb.getMusicas()) {
                    if (mus.getFilePath() != null && mus.getUsers().contains(username)) {
                        replyMap.put(String.format("music_%d", music_count), mus.getTitulo());
                        music_count++;
                    }
                }
            }
        }

        replyMap.put("music_count", String.valueOf(music_count));
        buffer = Protocol.sendHashMapSuccess(replyMap, hash);

        // enviar sempre visto que só este servidor é que tem o ficheiro
        try {
            MulticastSocket socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName(MulticastServer.MULTICAST_ADDRESS);
            socket.joinGroup(group);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MulticastServer.MULTICAST_PORT);
            System.out.println("handler: sending reply...");
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método de tratamento de um pedido de partilha de um ficheiro de musica.
     */
    private void shareSong() {
        byte[] buffer = null;
        String hash = map.get("hash");
        String username = map.get("username");
        String songName = map.get("music_title");

        Utilizador user = getUser(username);

        if (user != null) {
            for (Artista art : artistas) {
                for (Album alb : art.getAlbuns()) {
                    for (Musica mus : alb.getMusicas()) {
                        if (mus.equals(songName)) {
                            mus.addUser(username);
                            buffer = Protocol.success(String.format("Ficheiro partilhado com '%s'.", username), hash);
                        }
                    }
                }
            }
        }

        if (buffer == null) {
            buffer = Protocol.error("Nao é possivel partilhar o ficheiro.", hash);
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de notificacao de um utilizador que esteja offline.
     * A notificaçao é guardada num array para quando o utilizador fizer login receber todas as notificaçoes
     */
    private void offlineNotify() {
        byte[] buffer;
        String offline_user = map.get("username");
        String notification = map.get("msg");
        String hash = map.get("hash");
        ArrayList<String> aux = offlineNotifications.get(offline_user);
        if (aux == null) {
            aux = new ArrayList<String>();
        }
        aux.add(notification);
        offlineNotifications.put(offline_user, aux);
        buffer = Protocol.success("Notificado offline", hash);
        sendReply(buffer);
    }

    /**
     * Método que envia todas as notificacoes que um dado utilizador recebeu enquato estava offline
     */
    private void getNotifications() {
        byte[] buffer;
        HashMap<String, String> replyMap = new HashMap<>();
        String username = map.get("username");
        String hash = map.get("hash");
        ArrayList aux = offlineNotifications.get(username);
        if (aux != null) {
            int size = aux.size();
            replyMap.put("not_count", String.valueOf(size));
            for (int i = 0; i < size; i++) {
                replyMap.put(String.format("not_%d", i), aux.get(i).toString());
            }
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        } else {
            replyMap.put("not_count", "0");
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        }
        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de procura de música por nome.
     */
    private void searchMusic() {
        byte[] buffer;
        boolean found = false;
        HashMap<String, String> replyMap = new HashMap<>();
        String name = map.get("name");
        String hash = map.get("hash");

        for (Artista artista : artistas) {
            for (Album album : artista.getAlbuns()) {
                for (Musica musica : album.getMusicas()) {
                    if (musica.equals(name)) {
                        found = true;
                        replyMap.put("title", musica.getTitulo());
                    }
                }
            }
        }
        if (found) {
            buffer = Protocol.sendHashMapSuccess(replyMap, hash);
        } else {
            replyMap.put("msg", String.format("Música %s não encontrada.", name));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        }
        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de procura de album por nome.
     */
    private void searchAlbum() {
        byte[] buffer;
        boolean found = false;
        HashMap<String, String> replyMap = new HashMap<>();
        String name = map.get("name");
        String hash = map.get("hash");
        for (Artista aux : artistas) {
            for (Album aux_album : aux.getAlbuns()) {
                String titulo = aux_album.getTitulo();
                if (titulo.equals(name)) {
                    found = true;
                    ArrayList<Musica> musicas = aux_album.getMusicas();
                    ArrayList<Critica> criticas = aux_album.getCriticas();
                    int music_count = musicas.size();
                    int critic_count = criticas.size();
                    replyMap.put(("artista"), aux.getNome());
                    replyMap.put(("album"), aux_album.getTitulo());
                    replyMap.put(("genero"), aux_album.getGeneroMusical());
                    replyMap.put(("music_count"), String.valueOf(music_count));
                    replyMap.put(("critic_count"), String.valueOf(critic_count));

                    for (int i = 0; i < music_count; i++) {
                        replyMap.put(String.format("music_%d", i), musicas.get(i).getTitulo());
                    }
                    for (int i = 0; i < critic_count; i++) {
                        replyMap.put(String.format("critic_%d_pont", i), String.valueOf(criticas.get(i).getPontuacao()));
                        replyMap.put(String.format("critic_%d_just", i), String.valueOf(criticas.get(i).getJustificacao()));
                    }
                }
            }
        }
        if (!found) {
            replyMap.put("msg", String.format("Album %s não foi encontrado.", name));
            buffer = Protocol.sendHashMapError(replyMap, hash);
        } else {
            buffer = Protocol.sendHashMapSuccess(replyMap, map.get("hash"));
        }
        sendReply(buffer);

    }

    /**
     * Método de tratamento de um pedido de criação de um artista.
     */
    private void addArtist() {
        byte[] buffer;
        String name = map.get("name");
        if (artistExists(name)) {
            buffer = Protocol.error("Artist already exists.", map.get("hash"));
        } else {
            Artista artista = new Artista(name);
            artistas.add(artista);
            buffer = Protocol.success("Artist created", map.get("hash"));
        }

        sendReply(buffer);
    }

    /**
     * Método de tratamento do registo de um novo utilizador
     */
    private void userRegister() {
        byte[] buffer;
        String username = map.get("username");
        String password = map.get("password");
        String hash = map.get("hash");

        if (getUser(username) != null) {
            buffer = Protocol.error("Utilizador já existe.", hash);
        } else {
            Utilizador user = new Utilizador(username, password);
            if (utilizadores.size() == 0) {
                user.setIsEditor(true);
                user.setIsAdmin(true);
            }
            utilizadores.add(user);
            buffer = Protocol.loginSuccess(user, hash);
        }
        sendReply(buffer);
    }

    /**
     * Método de tratamento do registo de um novo utilizador usando dropbox
     */
    private void userDropboxRegister() {
        byte[] buffer;
        String username = map.get("username");
        String account_id = map.get("account_id");
        String access_token = map.get("access_token");
        String hash = map.get("hash");

        if (getUser(username) != null) {
            buffer = Protocol.error("Utilizador já existe.", hash);
        } else {
            Utilizador user = new Utilizador(username);
            if (utilizadores.size() == 0) {
                user.setIsEditor(true);
                user.setIsAdmin(true);
            }
            user.setAccount_id(account_id);
            user.setAccess_token(access_token);
            utilizadores.add(user);
            buffer = Protocol.loginSuccess(user, hash);
        }
        sendReply(buffer);
    }


    /**
     * Método de tratamento de um pedido de login de um utilizador.
     */
    private void userLogin() {
        byte[] buffer;
        String username = map.get("username");
        String password = map.get("password");
        String hash = map.get("hash");
        Utilizador utilizador = getUser(username);

        if (utilizador == null) {
            buffer = Protocol.loginError("Utilizador nao existe.", hash);
        } else {
            if (utilizador.checkPassword(password)) {
                buffer = Protocol.loginSuccess(utilizador, hash);
            } else {
                buffer = Protocol.loginError("Password errada.", hash);
            }
        }
        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de login de um utilizador.
     */
    private void userLoginAccountId() {
        byte[] buffer;
        String account_id = map.get("account_id");
        String hash = map.get("hash");
        Utilizador utilizador = null;

        for (Utilizador aux : utilizadores) {
            if (aux.getAccount_id().equals(account_id)) {
                utilizador = aux;
            }
        }

        if (utilizador == null) {
            buffer = Protocol.loginError("Utilizador nao existe.", hash);
        } else {
            buffer = Protocol.loginSuccess(utilizador, hash);
        }
        sendReply(buffer);
    }

    /**
     * Método de tratamento de um pedido de atualizacao de account_id de um utilizador.
     */
    private void setAccountId() {
        byte[] buffer;
        String account_id = map.get("account_id");
        String access_token = map.get("access_token");
        String username = map.get("username");
        String hash = map.get("hash");
        Utilizador utilizador = getUser(username);

        if (utilizador == null) {
            buffer = Protocol.loginError("Utilizador nao existe.", hash);
        } else {
            utilizador.setAccount_id(account_id);
            utilizador.setAccess_token(access_token);
            buffer = Protocol.success("account_id atualizado", map.get("hash"));
        }
        sendReply(buffer);
    }

    /**
     * Método auxiliar para guardar em disco as listas de utilizadores e de artistas.
     */
    synchronized private void saveIntoStorage() {
        FicheiroDeObjectos storage = new FicheiroDeObjectos();
        try {
            storage.abreEscrita(MulticastServer.ARTISTAS_FILE);
            storage.escreveObjecto(artistas);
            storage.fechaEscrita();
            storage.abreEscrita(MulticastServer.UTILIZADORES_FILE);
            storage.escreveObjecto(utilizadores);
            storage.fechaEscrita();
        } catch (IOException e) {
            System.out.println("Erro a escrever em ficheiro.");
        }
    }
}