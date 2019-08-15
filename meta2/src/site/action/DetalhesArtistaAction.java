package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Artista;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Ação para mostrar os detalhes de um artista.
 */

public class DetalhesArtistaAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private Artista artista;
    private String nome;

    /**
     * Dado um nome de um artista, procura na lista de artistas devolvida pelo servidor RMI
     * o artista de nome "nome", guardando-o na sessão para as ações de edição/remoção.
     * @return ACTIONRESULT
     */
    @Override
    public String execute() {
        try {
            ArrayList<Artista> artistas = this.getUserBean().getArtistas();
            for(Artista aux : artistas) {
                if(aux.equals(this.nome)) {
                    this.artista = aux;
                    session.put("artista_atual", artista);
                    return SUCCESS;
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Getter do artista
     *
     * @return artista
     */
    public Artista getArtista() {
        return artista;
    }

    /**
     * Setter do nome do artista atual
     *
     * @param nome nome do artista
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
