package protocol;

import models.Utilizador;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Class onde está definido o protocolo de comunicação com o servidor Multicast
 */

public class Protocol {
    /**
     * Método para a conversão do pedido de login num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param username String username
     * @param password String password
     * @return packet do pedido
     */
    public static byte[] loginRequest(String username, String password) {
        String payload = String.format("type|%s;username|%s;password|%s", Type.LOGIN, username, password);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido de login num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param account_id String account_id
     * @return packet do pedido
     */
    public static byte[] loginRequest(String account_id) {
        String payload = String.format("type|%s;account_id|%s", Type.LOGIN_ACCOUNT_ID, account_id);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido de registo num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param username String username
     * @param password String password
     * @return packet do pedido
     */
    public static byte[] registerRequest(String username, String password) {
        String payload = String.format("type|%s;username|%s;password|%s", Type.REGISTER, username, password);
        return appendRandomHash(payload).getBytes();
    }


    public static byte[] dropboxRegister(String account_id, String username, String access_token) {
        String payload = String.format("type|%s;username|%s;account_id|%s;access_token|%s", Type.DROPBOX_REGISTER, username, account_id, access_token);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão da mensagem de loginErro num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param errorMessage String errorMessage
     * @param hash         String hash do pedido
     * @return packet do pedido
     */
    public static byte[] loginError(String errorMessage, String hash) {
        String message = String.format("type|%s;msg|%s", Type.LOGIN_ERROR, errorMessage);
        return appendHash(message, hash).getBytes();
    }

    /**
     * Método para a conversão da mensagem de loginSucess num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param user Utilizador loggado
     * @param hash String hash do pedido
     * @return packet do pedido
     */
    public static byte[] loginSuccess(Utilizador user, String hash) {
        String message = String.format("type|%s;username|%s;isEditor|%s;isAdmin|%s;account_id|%s;access_token|%s", Type.LOGIN_SUCCESS,
                user.getUsername(), user.getIsEditor(), user.getIsAdmin(), user.getAccount_id(), user.getAccess_token());
        return appendHash(message, hash).getBytes();
    }

    /**
     * Método para a conversão do pedido searchArtistRequest num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param nome String nome do artista a procurar
     * @return packet do pedido
     */
    public static byte[] searchArtistRequest(String nome) {
        String payload = String.format("type|%s;nome|%s", Type.SEARCH_ARTIST, nome);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido createArtist num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param nome String nome do artista a criar
     * @return packet do pedido
     */
    public static byte[] createArtist(String nome) {
        String payload = String.format("type|%s;nome|%s", Type.CREATE_ARTIST, nome);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido removeArtist num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param nome String nome do artista a remover
     * @return packet do pedido
     */
    public static byte[] removeArtist(String nome) {
        String payload = String.format("type|%s;nome|%s", Type.REMOVE_ARTIST, nome);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido removeAlbum num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param nome String nome do album a remover
     * @return packet do pedido
     */
    public static byte[] removeAlbum(String nome) {
        String payload = String.format("type|%s;nome|%s", Type.REMOVE_ALBUM, nome);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido removeSong num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param nome String nome da musica remover
     * @return packet do pedido
     */
    public static byte[] removeSong(String nome) {
        String payload = String.format("type|%s;nome|%s", Type.REMOVE_SONG, nome);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido createAlbum num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param titulo  String nome do album a criar
     * @param artista String nome do artista autor do album
     * @param genero  String genero musical do album a criar
     * @return packet do pedido
     */
    public static byte[] createAlbum(String titulo, String artista, String genero) {
        String payload = String.format("type|%s;title|%s;artist|%s;genre|%s", Type.CREATE_ALBUM, titulo, artista, genero);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido createSong num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param songName  String nome da musica a criar
     * @param albumName String nome do album a associar
     * @return packet do pedido
     */
    public static byte[] createSong(String songName, String albumName) {
        String payload = String.format("type|%s;title|%s;album|%s", Type.CREATE_SONG, songName, albumName);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido searchAlbumRequest num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param nome String nome do album a procurar
     * @return packet do pedido
     */
    public static byte[] searchAlbumRequest(String nome) {
        String payload = String.format("type|%s;name|%s", Type.SEARCH_ALBUM, nome);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido getAlbumDetails num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param album String nome do album a pesquisar
     * @return packet do pedido
     */
    public static byte[] getAlbumDetails(String album) {
        String payload = String.format("type|%s;album|%s", Type.ALBUM_DETAILS, album);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido searchMusicaRequest num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param nome String nome da musica a pesquisar
     * @return packet do pedido
     */
    public static byte[] searchMusicaRequest(String nome) {
        String payload = String.format("type|%s;name|%s", Type.SEARCH_MUSIC, nome);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido makeEditor num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param username String nome do album a criar
     * @return packet do pedido
     */
    public static byte[] makeEditor(String username) {
        String payload = String.format("type|%s;username|%s", Type.MAKE_EDITOR, username);
        return appendRandomHash(payload).getBytes();
    }

    public static byte[] getUsers() {
        String payload = String.format("type|%s", Type.ALL_USERS);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido createCritique num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param titulo    String nome do album a criticar
     * @param pontuacao String pontuacao da critica
     * @param critica   String justificacao da critica
     * @return packet do pedido
     */
    public static byte[] createCritique(String titulo, String pontuacao, String critica) {
        String payload = String.format("type|%s;title|%s;points|%s;msg|%s", Type.CREATE_CRITIQUE, titulo, pontuacao, critica);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido listArtist num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @return packet do pedido
     */
    public static byte[] listArtist() {
        String payload = String.format("type|%s", Type.LIST_ARTIST);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão da mensagem success num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param successMessage String nome do album a criar
     * @param hash String hash do pedido (identificador)
     * @return packet do pedido
     */
    public static byte[] success(String successMessage, String hash) {
        String message = String.format("type|%s;msg|%s", Type.SUCCESS, successMessage);
        return appendHash(message, hash).getBytes();
    }

    /**
     * Método para a conversão da mensagem error num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param errorMessage String mensagem de erro
     * @param hash String hash do pedido (identificador)
     * @return packet do pedido
     */
    public static byte[] error(String errorMessage, String hash) {
        String message = String.format("type|%s;msg|%s", Type.ERROR, errorMessage);
        return appendHash(message, hash).getBytes();
    }

    /**
     * Método para a conversão do HashMap de sucesso num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param map HashMap com informações relevantes ao pedido.
     * @param hash String hash do pedido (identificador)
     * @return packet do pedido
     */
    public static byte[] sendHashMapSuccess(HashMap<String, String> map, String hash) {
        String payload = String.format("type|%s", Type.SUCCESS);
        payload += ";" + mapToString(map);
        return appendHash(payload, hash).getBytes();
    }

    /**
     * Método para a conversão do HashMap de erro num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param map HashMap com informações relevantes ao pedido.
     * @param hash String hash do pedido (identificador)
     * @return packet do pedido
     */
    public static byte[] sendHashMapError(HashMap<String, String> map, String hash) {
        String payload = String.format("type|%s", Type.ERROR);
        payload += ";" + mapToString(map);
        return appendHash(payload, hash).getBytes();
    }

    /**
     * Método para a conversão do pedido uploadSong num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param songName String nome da musica para upload
     * @param username String nome do utilizador que vai fazer upload
     * @return packet do pedido
     */
    public static byte[] uploadSong(String songName, String username) {
        String payload = String.format("type|%s;music_title|%s;username|%s", Type.UPLOAD_MUSIC, songName, username);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido downloadSong num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param songName String nome da musica para upload
     * @param username String nome do utilizador que vai fazer download
     * @return packet do pedido
     */
    public static byte[] downloadSong(String songName, String username) {
        String payload = String.format("type|%s;music_title|%s;username|%s", Type.DOWNLOAD_MUSIC, songName, username);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido getDownloadableSongs num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param username String nome do utilizador
     * @return packet do pedido
     */
    public static byte[] getDownloadableSongs(String username) {
        String payload = String.format("type|%s;username|%s", Type.GET_DOWNLOADABLE_SONGS, username);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido shareSong num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param musica String musica a partilhar
     * @param username String nome do utilizador com quem partilhar
     * @return packet do pedido
     */
    public static byte[] shareSong(String musica, String username) {
        String payload = String.format("type|%s;music_title|%s;username|%s", Type.SHARE_SONG, musica, username);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido offlineNotifications num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param username String nome do utilizador
     * @param msg String mensagem de notificação
     * @return packet do pedido
     */
    public static byte[] offlineNotifications(String username, String msg) {
        String payload = String.format("type|%s;username|%s;msg|%s", Type.OFFLINE_NOTS, username, msg);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido editAlbumDesc num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param album String album a editar
     * @param username String nome do utilizador
     * @param msg String mensagem de notificação
     * @return packet do pedido
     */
    public static byte[] editAlbumDesc(String album, String msg, String username) {
        String payload = String.format("type|%s;album|%s;msg|%s;username|%s", Type.EDIT_ALBUM_DESC, album, msg, username);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido editAlbumDesc num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param oldName String album a editar
     * @param newName String nome do utilizador
     * @return packet do pedido
     */
    public static byte[] editSong(String oldName, String newName) {
        String payload = String.format("type|%s;oldname|%s;newname|%s", Type.EDIT_SONG, oldName, newName);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido getNotifications num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     * @param username String nome do utilizador
     * @return packet do pedido
     */
    public static byte[] getNotifications(String username) {
        String payload = String.format("type|%s;username|%s", Type.GET_NOTS, username);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão do pedido atualizaçao de album num array de bytes.
     * Dados os parâmetros de entrada, cria um hashmap com a informação necessária ao pedido, retornando
     * um Array de bytes com a informação.
     *
     * @param old    nome do album
     * @param titulo novo titulo
     * @param genero novo genero
     * @return packet do pedido
     */
    public static byte[] updateAlbum(String old, String titulo, String genero) {
        String payload = String.format("type|%s;old_title|%s;new_title|%s;genre|%s", Type.UPDATE_ALBUM, old, titulo, genero);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão de um pedido de atualizacao da account id de um utilizador num array de bytes
     *
     * @param username   username
     * @param account_id account_id
     * @param access_token access_token
     * @return packet do pedido
     */
    public static byte[] setAccountId(String username, String account_id, String access_token) {
        String payload = String.format("type|%s;username|%s;account_id|%s;access_token|%s", Type.SET_ACCOUNT_ID, username, account_id, access_token);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método para a conversão de um pedido de atualizacao do id do ficheiro da musica num array de bytes
     *
     * @param songName   nome
     * @param albumName  nome do album
     * @param artistName nome do artista
     * @param songId     songId
     * @return packet do pedido
     */
    public static byte[] setSongId(String songName, String albumName, String artistName, String songId) {
        String payload = String.format("type|%s;songName|%s;albumName|%s;artistName|%s;songId|%s", Type.SET_SONG_ID, songName, albumName, artistName, songId);
        return appendRandomHash(payload).getBytes();
    }

    /**
     * Método obter o identificador(hash) de um pedido.
     * Sendo a hash acrescentada no final da mensagem do pedido, procura no pacote o valor depois do ultimo "|"
     * @param payload byte Array com a informação do pacote
     * @return hash (identificador) do pedido
     */
    public static String getHash(byte[] payload) {
        String aux = new String(payload);
        return aux.substring(aux.lastIndexOf("|") + 1);
    }

    /**
     * Método obter o identificador(hash) de um pedido.
     * Sendo o tipo acrescentado no inicio da mensagem do pedido, procura a mensagem entre o primeiro "|" e o primeiro ";"
     * @param payload byte Array com a informação do pacote
     * @return Type tipo da mensagem
     */
    public static Type getType(byte[] payload) {
        String aux = new String(payload);
        aux = aux.substring(aux.indexOf("|") + 1, aux.indexOf(";"));
        return Type.valueOf(aux);
    }

    /**
     * Método para obter o correspondente HashMap a partir do pacote.
     * @param payload String mensagem do pacote
     * @return HashMap de pares chave valor do pacote
     */
    public static HashMap<String, String> getHashMap(String payload) {
        HashMap<String, String> map = new HashMap<>();

        String[] nameValuePairs = payload.split(";");
        try {
            for (String nameValuePair : nameValuePairs) {
                String[] nameValue = nameValuePair.split("\\|");
                map.put(nameValue[0], nameValue[1]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            map.clear();
            map.put("type", String.valueOf(Type.INVALID));
            map.put("hash", "_");
        }

        return map;
    }

    /**
     * Método de conversão do HashMap em String.
     * @param map hashmap a converter
     * @return String HashMap
     */
    private static String mapToString(HashMap<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(";");
            }
            String value = map.get(key);
            stringBuilder.append(key);
            stringBuilder.append("|");
            stringBuilder.append(value);
        }

        return stringBuilder.toString();
    }

    /**
     * source: https://www.baeldung.com/sha-256-hashing-java
     * Método para acrescentar à mensagem um identificador (hash)
     * @param payload payload da mensagem
     * @return nova payload de mensagem com o identificador adicionado
     */
    private static String appendRandomHash(String payload) {
        MessageDigest digest;
        byte[] hash = new byte[10];
        try {
            digest = MessageDigest.getInstance("SHA-256");
            hash = UUID.randomUUID().toString().getBytes();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return appendHash(payload, Protocol.bytesToHex(hash));
    }

    private static String appendHash(String payload, byte[] hash) {
        return payload + String.format(";hash|%s", new String(hash));
    }

    private static String appendHash(String payload, String hash) {
        return Protocol.appendHash(payload, hash.getBytes());
    }

    /**
     * source: https://www.baeldung.com/sha-256-hashing-java
     *
     * @param hash hash em bytes
     * @return hash em string (hex)
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte aHash : hash) {
            String hex = Integer.toHexString(0xff & aHash);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

