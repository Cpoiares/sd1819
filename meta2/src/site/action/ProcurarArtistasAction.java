package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Artista;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * CLasse do acao de produrar artistas dado um nome parcial
 */
public class ProcurarArtistasAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private ArrayList<Artista> lista;
    private String nome;

    /**
     * Metodo da acao de procura de artistas
     *
     * @return resultado da acao
     */
    @Override
    public String execute() {
        this.lista = new ArrayList<>();
        try {
            ArrayList<Artista> artistas = this.getUserBean().getArtistas();
            for (Artista aux : artistas) {
                if (aux.getNome().toUpperCase().contains(this.nome.toUpperCase())) {
                    this.lista.add(aux);
                }
            }

        } catch (RemoteException | NullPointerException e) {
            e.printStackTrace();
            return INPUT;
        }

        return SUCCESS;
    }

    /**
     * Getter da lista de artistas resultantes
     *
     * @return lista de artistas
     */
    public ArrayList<Artista> getArtistas() {
        return this.lista;
    }

    /**
     * Setter do nome do artista
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
