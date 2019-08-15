package site.model;

import client.RMIClientInterface;
import models.*;
import server.NullObjectException;
import server.RMIServerInterface;
import site.ws.WebSocketAnnotation;

import javax.rmi.CORBA.Util;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Classe de Bean de utilizador.
 */
public class UserBean extends UnicastRemoteObject implements RMIClientInterface {
    private RMIServerInterface server;
    private Utilizador utilizador;
    private String username;
    private String password;
    private String account_id;

    /**
     * Construtor por omissão.
     * @throws RemoteException
     */
    public UserBean() throws RemoteException {
        super();
        try {
            server = (RMIServerInterface) Naming.lookup("server");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para registar um cliente.
     * Envia ao servidor RMI uma referência do Bean do utilizador, o username e a sua password,
     * que lhe devolve um novo Utilizador. Em caso de sucesso procede a chamar o método subscribe
     * no servidor RMI.
     * @return Utilizador(callback, username, password) em caso de sucesso, null em casa de erro
     * @throws RemoteException
     */
    public boolean registerClient() throws RemoteException {
        this.utilizador = server.register(this, this.username, this.password);
        if (this.utilizador != null) {
            server.subscribe(this.username, this);
        }
        return this.utilizador != null;
    }

    /**
     * Método para tornar um utilizador editor.
     * Ao receber o nome do username, chama o método makeEditor no servidor RMI.
     * @param username
     * @return true no caso de sucesso, false no caso de erro.
     * @throws RemoteException
     */
    public boolean makeEditor(String username) throws RemoteException {
        String message = server.makeEditor(username);
        // Retorna true se não der erro
        return !message.split("\\|")[0].equals("ERROR");
    }

    /**
     * Método para adicionar uma critica a um dado album.
     * Recebendo um título, uma pontuação e uma justificação da crítica, chama o método createCritique no servidor RMI
     * @param titulo titulo do album
     * @param pontuacao pontuação da critica
     * @param justificacao justificação da crítica
     * @return true no caso de sucesso, false no caso de erro.
     * @throws RemoteException
     */
    public boolean addCritique(String titulo, String pontuacao, String justificacao) throws RemoteException {
        try {
             server.createCritique(titulo, pontuacao, justificacao);
             return true;
        }catch(NullObjectException| ExceedsMaxLengthException e){
            return false;
        }
    }

    /**
     * Método para verificar o par username password.
     * Ao chamar o método login no servidor com o username e password do utilizaodr assim como a sua referência
     * se o utilizador devolvido existir, subscreve o servidor RMI ao chamar o método subscribe no servidor RMI.
     * @return true no caso de sucesso, false no caso de erro.
     * @throws RemoteException
     */
    public boolean getUserMatchesPassword() throws RemoteException {
        this.utilizador = server.login(this, this.username, this.password);
        if (this.utilizador != null) {
            server.subscribe(this.username, this);
        }
        return this.utilizador != null;
    }

    /**
     * Método para login dado um account id da dropbox.
     * Ao chamar o método login no servidor RMI com apenas o identificador da conta, se o utilizador devolvido exisitr
     * atualiza o username, o id da conta Dropbox e subscreve o servidor RMI através do método subscribe.
     * @param account_id Id de conta Dropbox
     * @return true no caso de sucesso, false no caso de erro.
     */
    public boolean dropboxLogin(String account_id) {
        this.utilizador = null;
        try {
            this.utilizador = server.login(account_id);
            if (this.utilizador != null) {
                this.username = utilizador.getUsername();
                server.subscribe(this.username, this);
                this.account_id = account_id;
                return true;
            }
            return false;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método para o registo na plataforma com conta Dropbox.
     * Através de id de conta Dropbox, um username e um código de acesso, chama o métoodo registo no servidor RMI
     * com estes parâmetros. Caso o utilizador devolvido pelo servidor RMI exista, subscreve o servidor RMI através
     * método subscribe.
     * @param account_id Identificador de conta dropbox
     * @param username Username do utilizador
     * @param access_token Token de acesso dropbox
     * @return true no caso de sucesso, false no caso de erro.
     */
    public boolean dropboxRegister(String account_id, String username, String access_token) {
        this.utilizador = null;
        try {
            this.utilizador = server.register(account_id, username, access_token);
            if (this.utilizador != null) {
                this.username = utilizador.getUsername();
                server.subscribe(this.username, this);
                this.account_id = account_id;
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Método para guardar o id de conta Dropbox.
     * Chama o método setAccountID no servidor RMI com o nome de utilizador, id de conta dropbox e o código de acesso
     * atualizando também o id de conta dropbox no Bean do utilizador.
     * @param account_id String id de conta Dropbox
     * @param access_token String token de acesso dropbox
     */
    public void setAccount_id(String account_id, String access_token) {
        try {
            server.setAccountId(this.username, account_id, access_token);
            this.account_id = account_id;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para guardar o identificador dropbox de uma música.
     * Recebendo um nome de uma música, um nome de album, um artista autor e um identificador de uma música na Dropbox
     * chama o método setSongId no servidor RMI
     * @param musica nome da música
     * @param album nome do album
     * @param artista nome do autor do album
     * @param songId Identificador da música na dropbox.
     */
    public void setSongId(String musica, String album, String artista, String songId) {
        try {
            server.setSongId(musica, album, artista, songId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método getArtistas.
     * Chama o método listArtist no servidor RMI e devolve o Arraylist devolvido.
     * @return Arraylist de artistas
     * @throws RemoteException
     */
    public ArrayList<Artista> getArtistas() throws RemoteException {
        ArrayList<Artista> artistas;
        try {
            artistas = server.listArtist();
        } catch (NullObjectException e) {
            artistas = null;
        }
        return artistas;
    }

    /**
     * Método getMusica
     * Dado um nome de artista, nome do album e o titulo da música, procura na lista de artistas devolvida
     * pelo método getArtistas do UserBean o artista de nome "artistaNome" e devolve a música de nome "titulo" no album "albumNome"
     * @param artistaNome Nome do artista
     * @param albumNome Nome do album
     * @param titulo Nome da musica
     * @return Musica musicaout
     */
    public Musica getMusica(String artistaNome, String albumNome, String titulo) {
        ArrayList<Artista> artistas = null;
        try {
            artistas = this.getArtistas();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        assert artistas != null;
        for (Artista artista : artistas) {
            if (artista.equals(artistaNome)) {
                for (Album album : artista.getAlbuns()) {
                    if (album.equals(albumNome)) {
                        for (Musica musica : album.getMusicas()) {
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
     * Getter do username
     *
     * @return username
     * @throws RemoteException exception
     */
    public String getUsername() throws RemoteException {
        return this.username;
    }

    /**
     * Setter do username
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Dado um nome de artista procura na lista de artistas devolvida
     * pelo método getArtistas do UserBean o artista
     *
     * @param nome nome do artista
     * @return artista
     */
    public Artista searchArtista(String nome) throws RemoteException {
        Artista artista;
        try {
            artista = server.searchArtista(nome);
        } catch (NullObjectException e) {
            artista = null;
        }
        return artista;
    }

    /**
     * Edita a descricao de um album notifica os outros utilizadores.
     *
     * @param titulo    titulo do album
     * @param descricao nova descricao
     * @param username  editor que modificou
     * @return verdadeiro em caso de sucesso
     * @throws RemoteException exception
     */
    public boolean editAlbumDesc(String titulo, String descricao, String username) throws RemoteException {
        try{
            server.editAlbumDesc(titulo, descricao, username);
        }catch (RemoteException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Método de adicionar um aritsta novo no servidor rmi
     *
     * @param nome nome do artista
     * @return true
     * @throws RemoteException exception
     */
    public boolean adicionarArtista(String nome) throws RemoteException {
        try {
            server.createArtist(nome);
        } catch (NullObjectException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Adicionar album a um artista
     *
     * @param titulo  titulo do album
     * @param artista nome do artista
     * @param genero  genero do album
     * @return verdadeiro em caso de sucesso
     * @throws RemoteException exception
     */
    public boolean adicionarAlbum(String titulo, String artista, String genero) throws RemoteException {
        try {
            return server.createAlbum(titulo, artista, genero) != null;
        } catch (NullObjectException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método para listar todos os utilizadores, utilizdo para mostrar uma lista de utilizadores para tornar editor
     *
     * @return lista de users
     * @throws RemoteException exception
     */
    public ArrayList<String> getNonEditors() throws RemoteException {
        ArrayList<String> usernames = new ArrayList<>();
        ArrayList<Utilizador> users;
        try{
            users = server.getAllUsers();
            for (Utilizador user : users) {
                if (!user.getIsEditor()) {
                    usernames.add(user.getUsername());
                }
            }
        } catch(NullObjectException e){
            usernames = null;
        }
        return usernames;
    }

    /**
     * Método para listar todos os utilizadores com conta dropbox
     *
     * @return lista de users
     * @throws RemoteException exception
     */
    public ArrayList<Utilizador> getDropboxUsers() throws RemoteException {
        ArrayList<Utilizador> dropboxUsers = new ArrayList<>();
        ArrayList<Utilizador> users;
        try {
            users = server.getAllUsers();
            for (Utilizador user : users) {
                if (user.getAccount_id().length() > 3) {
                    dropboxUsers.add(user);
                }
            }
        } catch (NullObjectException e) {
            users = null;
            dropboxUsers = null;
        }
        return dropboxUsers;
    }

    /**
     * Setter de password
     *
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter de utilizador
     *
     * @return utilizador
     */
    public Utilizador getUtilizador() {
        return utilizador;
    }

    /**
     * Método para remover um artista dado o seu nome
     *
     * @param nome nome do artista
     * @return verdadeiro em caso de sucesso
     * @throws RemoteException exception
     */
    public boolean removeArtist(String nome) throws RemoteException {
        try{
            server.removeArtist(nome);
        } catch(RemoteException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Metodo para remover uma musica dado o seu nome
     *
     * @param nome nome da musica
     * @return verdadeiro em caso de sucesso
     * @throws RemoteException exception
     */
    public boolean removeSong(String nome) throws RemoteException {
        try{
            server.removeSong(nome);
        } catch(RemoteException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Método para remocao de um album dado o seu nome
     *
     * @param nome nome do album
     * @return verdadeiro em caso de sucesso
     * @throws RemoteException exception
     */
    public boolean removeAlbum(String nome) throws RemoteException {
        try{
            server.removeAlbum(nome);
        } catch(RemoteException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Metodo para adicionar uma musica
     *
     * @param titulo    nome da musica
     * @param albumNome nome do album
     * @return verdadeiro em caso de sucesso
     */
    public boolean adicionarMusica(String titulo, String albumNome) {
        try {
            return server.createSong(titulo, albumNome) != null;
        } catch (NullObjectException | RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Getter de account id
     *
     * @return account_id
     */
    public String getAccount_id() {
        return account_id;
    }

    /**
     * Setter de account_id
     *
     * @param account_id account_id
     */
    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    /**
     * Callback para dar permissoes ao utilizador
     *
     * @throws RemoteException exception
     */
    @Override
    public void givePermission() throws RemoteException {
        System.out.println("givePermission");
        this.utilizador.setIsEditor(true);
        WebSocketAnnotation.make_editor(this.username);
    }

    /**
     * Callback para impressao na consola (override necessario)
     *
     * @param message mensagem
     * @throws RemoteException exception
     */
    @Override
    public void print(String message) throws RemoteException {
        System.out.println(message);
    }
}
