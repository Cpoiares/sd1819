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
 * Classe da action de procura de albums dado um nome
 */
public class ProcurarAlbumsAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private ArrayList<Album> lista;
    private String nome;

    /**
     * Metodo da acao de procura de albums
     *
     * @return resultado da acao
     */
    @Override
    public String execute() {
        this.lista = new ArrayList<>();
        try {
            ArrayList<Artista> artistas = this.getUserBean().getArtistas();
            for (Artista artista : artistas) {
                for(Album album : artista.getAlbuns()) {
                    if (album.getTitulo().toUpperCase().contains(this.nome.toUpperCase())) {
                        this.lista.add(album);
                    }
                }

            }

        } catch (RemoteException | NullPointerException e) {
            return INPUT;
        }

        return SUCCESS;
    }

    /**
     * Getter de todos os albums
     *
     * @return arraylist de albums
     */
    public ArrayList<Album> getAlbums() {
        return this.lista;
    }

    /**
     * Setter do nome do album a procurar
     *
     * @param nome nome do album
     */
    public void setNome(String nome) {
        this.nome = nome;
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
}
