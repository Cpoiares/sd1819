package models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe Album
 */
public class Album implements Serializable {
    private ArrayList<Musica> musicas;
    private ArrayList<Critica> criticas;
    private String titulo;
    private Artista autor;
    private String generoMusical;
    private String albumDesc;
    private ArrayList<String> users;

    /**
     * Construtor por parâmetros
     *
     * @param titulo String titulo do album
     * @param autor  Artista autor do album
     */
    public Album(String titulo, Artista autor) {
        this.titulo = titulo;
        this.autor = autor;
        this.musicas = new ArrayList<>();
        this.criticas = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    /**
     * Construtor por parâmetros
     *
     * @param titulo        String titulo do album
     * @param autor         Artista autor do album
     * @param generoMusical String genero musical do album
     */
    public Album(String titulo, Artista autor, String generoMusical) {
        this.titulo = titulo;
        this.autor = autor;
        this.generoMusical = generoMusical;
        this.musicas = new ArrayList<>();
        this.criticas = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    /**
     * Setter -  user
     *
     * @param username String nome do utilizador a adicionar
     */
    public void setUser(String username) {
        this.users.add(username);
    }

    /**
     * Getter - Users
     *
     * @return ArrayList String editores do album
     */
    public ArrayList<String> getUsers() {
        return this.users;
    }

    /**
     * Getter - Album Description
     *
     * @return String descrição do album
     */
    public String getAlbumDesc() {
        return this.albumDesc;
    }

    /**
     * Setter - Album Description
     *
     * @param albumDesc String descrição a adicionar
     */
    public void setAlbumDesc(String albumDesc) {
        this.albumDesc = albumDesc;
    }

    /**
     * Setter - Album Description
     *
     * @param albumDesc String descrição do albúm
     * @param username  String username do editor
     */
    public void setAlbumDesc(String albumDesc, String username) {
        this.albumDesc = albumDesc;
        if (!this.users.contains(username)) {
            this.users.add(username);
        }
    }

    /**
     * Método para adicionar uma música ao album
     *
     * @param musica Musica musica a adicionar
     * @return boolean sucesso da operação.
     */
    public boolean addMusica(Musica musica) {
        return musicas.add(musica);
    }

    /**
     * Método para remover uma música do albúm.
     *
     * @param musica Musica musica a remover
     * @return boolean sucesso da operação
     */
    public boolean removeMusica(Musica musica) {
        return musicas.remove(musica);
    }

    /**
     * Método para remover uma música do albúm.
     *
     * @param nome String nome da música a remover
     * @return sucesso da operação.
     */
    public boolean removeMusica(String nome) {
        return musicas.remove(nome);
    }

    /**
     * Método para adicionar uma crítica ao album;
     *
     * @param critica Critica critica ao album.
     * @return boolean sucesso da operação;
     */
    public boolean addCritica(Critica critica) {
        return criticas.add(critica);
    }

    /**
     * Getter - Titulo
     * @return String titulo do album;
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Setter - Titulo
     * @param titulo String titulo do album;
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Getter - Autor
     * @return Artista autor do album;
     */
    public Artista getAutor() {
        return autor;
    }

    /**
     * Setter - Autor
     * @param autor Artista autor do album
     */
    public void setAutor(Artista autor) {
        this.autor = autor;
    }

    /**
     * Getter - Musicas
     * @return ArrayList Musica musicas do album;
     */
    public ArrayList<Musica> getMusicas() {
        return musicas;
    }

    /**
     * Getter - Musica
     * @param nome String musica a procurar
     * @return Musica musica procurada (null se não encontrar)
     */
    public Musica getMusica(String nome) {
        for (Musica musica : musicas) {
            if (musica.equals(nome)) {
                return musica;
            }
        }
        return null;
    }

    /**
     * Getter - Criticas
     * @return ArrayList Criticas criticas do album
     */
    public ArrayList<Critica> getCriticas() {
        return criticas;
    }

    /**
     * Setter - Criticas
     * @param criticas ArrayList Critica
     */
    public void setCriticas(ArrayList<Critica> criticas) {
        this.criticas = criticas;
    }

    /**
     * Getter - Genero Musical
     * @return String genero musical
     */
    public String getGeneroMusical() {
        return generoMusical;
    }

    /**
     * Setter - Genero Musical
     * @param generoMusical String genero musical do album
     */
    public void setGeneroMusical(String generoMusical) {
        this.generoMusical = generoMusical;
    }

    /**
     * Método de comparação de strings
     *
     * @param titulo a comparar
     * @return boolean igualdade
     */
    public boolean equals(String titulo) {
        return this.titulo.equals(titulo);
    }

    /**
     * Método de conversão do Album para String
     *
     * @return String com informação do artista
     */
    @Override
    public String toString() {
        return String.format("Album <autor: %s, titulo: %s, generoMusical: %s>", autor.getNome(), titulo, generoMusical);
    }
}
