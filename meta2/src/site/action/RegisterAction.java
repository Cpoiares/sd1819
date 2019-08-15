package site.action;

import com.opensymphony.xwork2.ActionSupport;
import site.model.UserBean;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Classe do metodo de registo de uma conta nova na plataforma DropMusic.
 */
public class RegisterAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String username = null, password = null;

    /**
     * Metodo da acao de registar uma conta nova com username e password
     *
     * @return resultado da acao
     */
    @Override
    public String execute() {
        session.clear();
        if (this.username != null && !username.equals("")) {
            this.getUserBean().setUsername(this.username);
            this.getUserBean().setPassword(this.password);
            try {
                if (this.getUserBean().registerClient()) {
                    session.put("username", this.getUserBean().getUtilizador().getUsername());
                    session.put("editor", this.getUserBean().getUtilizador().getIsEditor());
                    session.put("loggedin", true);
                    return SUCCESS;
                } else {
                    return LOGIN;
                }
            } catch (RemoteException e) {
                return LOGIN;

            }
        } else {
            return LOGIN;
        }
    }

    /**
     * Setter do username para registar
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Setter da password para registar
     *
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password;
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