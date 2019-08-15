package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Musica;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Ação para remover uma música.
 */
public class DeleteSongAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;

    /**
     * Ação para remover uma música. Através do nome da musica_atual guardado na sessão,
     * chama o método removeSong no RMI.
     * @return
     */
    @Override
    public String execute(){
        try{
            Musica aux = (Musica) session.get("musica_atual");
            if(aux!=null){
                this.getUserBean().removeSong(aux.getTitulo());
            }
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        return SUCCESS;
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
