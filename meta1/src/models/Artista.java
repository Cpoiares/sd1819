package models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class Artista
 */
public class Artista implements Serializable {
    ArrayList<Album> albuns;
    private String nome;

    /**
     * Construtor por parâmetros.
     *
     * @param nome String nome do artista
     */
    public Artista(String nome) {
        this.nome = nome;
        this.albuns = new ArrayList<>();
    }

    /**
     * Getter - Albuns
     *
     * @return ArrayList Album  Albuns do artista
     */
    public ArrayList<Album> getAlbuns() {
        return albuns;
    }

    /**
     * Setter - Albuns
     *
     * @param albuns Albuns do artista
     */
    public void setAlbuns(ArrayList<Album> albuns) {
        this.albuns = albuns;
    }

    /**
     * Método para adicionar um album à lista de albuns do artista
     *
     * @param album Album a adicionar
     * @return boolean sucesso da operação
     */
    public boolean addAlbum(Album album) {
        return this.albuns.add(album);
    }

    /**
     * Getter - Nome
     *
     * @return nome String nome do artista
     */
    public String getNome() {
        return this.nome;
    }

    /**
     * Setter - nome
     *
     * @param nome String nome do utilizador
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

  /*  public boolean findAlbum(String album) {
        for (Album aux : this.albuns) {
            if (aux.getTitulo().equals(album)) {
                return true;
            }
        }
        return false;
    }*/

    /**
     * Método de comparação de strings
     *
     * @param nome a comparar
     * @return boolean igualdade
     */
    public boolean equals(String nome) {
        return this.nome.equals(nome);
    }

    /**
     * Método de comparação de objectos artista
     *
     * @param artista a comparar
     * @return boolean igualdade
     */
    public boolean equals(Artista artista) {
        return this.nome.equals(artista.getNome());
    }

    /**
     * Método de conversão do Artista para String
     *
     * @return String com informação do artista
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(String.format("Artista<nome: %s> [", nome));
        for (Album album : albuns) {
            string.append(album);
        }
        string.append("]");

        return string.toString();
    }
}
