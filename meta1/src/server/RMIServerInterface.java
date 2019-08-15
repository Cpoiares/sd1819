package server;

import client.RMIClientInterface;
import models.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface RMIServerInterface extends Remote {

    Utilizador register(RMIClientInterface callback, String nome, String password) throws RemoteException;

    Utilizador register(String account_id, String username, String access_token) throws RemoteException;

    Utilizador login(RMIClientInterface callback, String nome, String password) throws RemoteException;

    String editSong(String oldName, String newName) throws RemoteException;

    String makeEditor(String username) throws RemoteException;

    Artista searchArtista(String nome) throws RemoteException, NullObjectException;

    Album searchAlbum(String nome) throws RemoteException, NullObjectException, ExceedsMaxLengthException;

    HashMap<String, String> uploadSong(String songName, String username) throws RemoteException;

    String editAlbumDesc(String album, String msg, String username) throws RemoteException;

    HashMap<String, String> downloadSong(String songName, String username) throws RemoteException;

    ArrayList<Musica> getDownloadableSongs(String username) throws RemoteException;

    Musica searchMusic(String nome) throws RemoteException, NullObjectException;

    boolean removeArtist(String nome) throws RemoteException;

    boolean removeAlbum(String nome) throws RemoteException;

    boolean removeSong(String nome) throws RemoteException;

    void subscribe(String name, RMIClientInterface c) throws RemoteException;

    void unsubscribe(String name) throws RemoteException;

    boolean setSongId(String songName, String albumName, String artistName, String songId) throws RemoteException;

    Artista createArtist(String nome) throws RemoteException, NullObjectException;

    Album createAlbum(String titulo, String artista, String genero) throws RemoteException, NullObjectException;

    Album getAlbumDetails(String album) throws RemoteException, NullObjectException, ExceedsMaxLengthException;

    Critica createCritique(String titulo, String pontuacao, String critica) throws RemoteException, NullObjectException, ExceedsMaxLengthException;

    Musica createSong(String parameter, String parameter1) throws RemoteException, NullObjectException;

    boolean isAlive() throws RemoteException;

    Utilizador login(String account_id) throws RemoteException;

    boolean setAccountId(String username, String account_id, String access_token) throws RemoteException;

    ArrayList<String> getNotifications(String username) throws RemoteException;

    ArrayList<Artista> listArtist() throws RemoteException, NullObjectException;

    String updateAlbum(String old_title, String new_title, String genero) throws RemoteException, NullObjectException;

    String shareSong(String musica, String username) throws RemoteException;

    ArrayList<Utilizador> getAllUsers() throws RemoteException, NullObjectException;
}
