package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Artista;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Ação para listar todos os albums (execute) e para adicionar um artista novo (adicionarArtista)
 */
public class ArtistasAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private ArrayList<Artista> artistas;

    private String nome = null;

    /**
     * Acao para listar todos os artistas. Usar o userBean para ir buscar todos os artistas ao servidor rmi.
     *
     * @return resultado da acao
     */
    @Override
    public String execute() {
        try {
            artistas = this.getUserBean().getArtistas();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return SUCCESS;
    }

    /**
     * Acao para adicionar um novo artista.
     *
     * @return resultado da acao
     */
    public String adicionarArtista() {
        System.out.println("AdicionarArtista action");
        if (this.nome != null && !this.nome.isEmpty()) {
            try {
                if (this.getUserBean().adicionarArtista(this.nome)) {
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
     * Setter do nome do novo artista
     *
     * @param nome nome do novo artista
     */
    public void setNome(String nome) {
        this.nome = nome;
    }
}
