package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Album;
import models.Artista;
import models.Critica;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;


/**
 * Ação para mostrar os detalhes de um álbum como músicas, críticas e pontuação média.
 */

public class DetalhesAlbumAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private Album album;
    private String albumNome;
    private String artistaNome;
    private String titulo;

    /**
     * Ação para mostrar os detalhes de um álbum. Através do nome do autor do album e do nome do album,
     * procura na lista de artistas devolvida pelo RMI o autor do album devolvendo o album com nome
     * "albumNome".
     * @return ACTIONRESULT
     */

    @Override
    public String execute() {
        try {
            ArrayList<Artista> artistas = this.getUserBean().getArtistas();
            for(Artista aux : artistas) {
                if (aux.equals(this.artistaNome)) {
                    for(Album aux2 : aux.getAlbuns()) {
                        System.out.println(aux2);
                        if (aux2.equals(this.albumNome)) {
                            this.album = aux2;
                            session.put("album_atual", album);
                            System.out.println(this.album);
                            return SUCCESS;
                        }
                    }
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Acao de remoção de uma musica de um album.
     *
     * @return resultado da acao
     */
    public String adicionarMusica() {
        System.out.println("adicionarMusica");
        if (titulo != null && !titulo.isEmpty()) {
            if (this.getUserBean().adicionarMusica(titulo, albumNome)) {
                return SUCCESS;
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
     * Getter do nome do album
     *
     * @return nome do album
     */
    public String getAlbumNome() {
        return albumNome;
    }

    /**
     * Setter do nome do album
     *
     * @param albumNome nome do album
     */
    public void setAlbumNome(String albumNome) {
        this.albumNome = albumNome;
    }

    /**
     * Getter do nome do album
     *
     * @return nome do album
     */
    public String getArtistaNome() {
        return artistaNome;
    }

    /**
     * Setter do nome do album
     *
     * @param artistaNome nome do album
     */
    public void setArtistaNome(String artistaNome) {
        this.artistaNome = artistaNome;
    }

    /**
     * Funçao auxiliar para obter a média da pontuacao de todas as criticas do album atual.
     *
     * @return media das criticas ao album
     */
    public String getMedia() {
        float average = 0;
        for(Critica i: this.album.getCriticas()){
            average += i.getPontuacao();
        }
        return String.valueOf(average/this.album.getCriticas().size());
    }

    /**
     * Getter do album
     *
     * @return album
     */
    public Album getAlbum() {
        return album;
    }

    /**
     * Setter do titulo do album
     *
     * @param titulo titulo do album
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
