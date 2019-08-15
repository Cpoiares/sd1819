package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Album;
import models.Artista;
import models.Musica;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Classe de metodo da acao de procura de musicas
 */
public class ProcurarMusicasAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private ArrayList<Musica> lista;
    private String nome;

    /**
     * Metodo da acao de procura de musicas
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
                    for(Musica musica : album.getMusicas()) {
                        if(musica.getTitulo().toUpperCase().contains(this.nome.toUpperCase())) {
                            lista.add(musica);
                        }
                    }
                }
            }

        } catch (RemoteException | NullPointerException e) {
            e.printStackTrace();
            return INPUT;
        }

        return SUCCESS;
    }

    /**
     * Getter da lista de musicas encontradas
     *
     * @return lista de musicas
     */
    public ArrayList<Musica> getMusicas() {
        return this.lista;
    }

    /**
     * Setter do nome da musica
     *
     * @param nome nome da musica
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
