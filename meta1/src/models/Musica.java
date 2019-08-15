package models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe Música
 */

public class Musica implements Serializable {
    private String titulo;
    private String filePath = " ";
    private ArrayList<String> users;
    /**
     * Construtor por parâmetros
     *
     * @param titulo String titulo da musica
     */
    public Musica(String titulo) {
        this.titulo = titulo;
        users = new ArrayList<>();
    }

    /**
     * Getter - FilePath
     *
     * @return String filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Setter - FilePath
     *
     * @param filePath String localização do ficheiro
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Método para adicionar um utilizador à lista de utilizadores da música.
     *
     * @param username String nome do utilizador
     * @return boolean resultado da operação
     */

    public boolean addUser(String username) {
        return users.add(username);
    }

    /**
     * Getter - users
     *
     * @return ArrayList users , lista de utilizadores da musica
     */
    public ArrayList<String> getUsers() {
        return this.users;
    }

    /**
     * Método de comparação de strings
     *
     * @param nome string nome a comparar
     * @return boolean igualdade
     */
    public boolean equals(String nome) {
        return this.titulo.equals(nome);
    }

    /**
     * Getter - Titulo
     *
     * @return String titulo
     */
    public String getTitulo() {
        return this.titulo;
    }

    /**
     * Setter - Titulo
     * @param titulo titulo
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}

