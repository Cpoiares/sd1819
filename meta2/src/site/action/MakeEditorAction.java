package site.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;
import site.ws.WebSocketAnnotation;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Classe da acao de tornar um utilizador editor.
 */
public class MakeEditorAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String username;

    /**
     * Metodo da acao de tornar um user editor.
     *
     * @return resultado da acao
     */
    @Override
    public String execute(){

        if(this.username != null){
            try{
                this.getUserBean().makeEditor(this.username);
                // WebSocketAnnotation.make_editor(this.username);
            } catch (RemoteException e){
                e.printStackTrace();
                return ERROR;
            }
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    /**
     * Setter do username do utilizador a tornar editor.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
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
