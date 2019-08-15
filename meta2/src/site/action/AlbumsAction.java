package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Album;
import models.Artista;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Ação para listar todos os albums (execute) e para adicionar um album novo (adicionarAlbum)
 */
public class AlbumsAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private ArrayList<Album> albums;

    private String titulo;
    private String artista;
    private String genero;

    /**
     * Acao para listar todos os albums. Usar o userBean para ir buscar todos os albums ao servidor rmi.
     *
     * @return resultado da acao
     */
    @Override
    public String execute() {
        try {
            albums = new ArrayList<>();
            ArrayList<Artista> artistas = this.getUserBean().getArtistas();
            for(Artista artista : artistas) {
                albums.addAll(artista.getAlbuns());
            }
            return SUCCESS;
        } catch (RemoteException e) {
            e.printStackTrace();
            return ERROR;
        }
    }

    /**
     * Acao para adicionar um novo album a um artista.
     *
     * @return resultado da acao
     */
    public String adicionarAlbum() {
        System.out.println("adicionarAlbum");
        if (titulo != null && !titulo.isEmpty() && genero != null && !genero.isEmpty() && artista != null && !artista.isEmpty()) {
            try {
                if (this.getUserBean().adicionarAlbum(titulo, artista, genero)) {
                    return SUCCESS;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return INPUT;
            }
        }

        return INPUT;
    }

    /**
     * Método auxiliar para ir buscar o userbean à sessao
     *
     * @return userBean
     */
    public UserBean getUserBean() {
        if (!session.containsKey("userBean")) {
            try {
                this.setUserBean(new UserBean());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return (UserBean) session.get("userBean");
    }

    /**
     * Método auxiliar para fazer set de um userBean na sessao
     *
     * @param userBean userbean
     */
    public void setUserBean(UserBean userBean) {
        this.session.put("userBean", userBean);
    }

    /**
     * Método para fazer ser da sessao (implements SessionAware)
     *
     * @param session sessao
     */
    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }


    /**
     * Getter do titulo
     *
     * @return titulo do album a inserir
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Setter do titulo
     *
     * @param titulo titulo do album a inserir
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Getter do nome do artista do album
     *
     * @return nome do artista
     */
    public String getArtista() {
        return artista;
    }

    /**
     * Setter do nome do artista do album
     *
     * @param artista nome do artista
     */
    public void setArtista(String artista) {
        this.artista = artista;
    }

    /**
     * Getter do genero musical do album a criar
     *
     * @return genero do album
     */
    public String getGenero() {
        return genero;
    }

    /**
     * Setter do genero musical do album a criar
     *
     * @param genero genero musical do album
     */
    public void setGenero(String genero) {
        this.genero = genero;
    }

    /**
     * Getter de todos os albums existentes
     *
     * @return Arraylist de albums
     */
    public ArrayList<Album> getAlbums() {
        return albums;
    }

}
