package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Album;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;
import site.ws.WebSocketAnnotation;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Classe de acao da edicao da descriçao de um album
 */
public class EditAlbumAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String descricao;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Metodo da acao de edicao da descricao de um album
     *
     * @return resultado da acao
     */
    @Override
    public String execute(){
        if(this.descricao != null && !this.descricao.isEmpty()){
            try{
                Album aux = (Album) session.get("album_atual");
                this.getUserBean().editAlbumDesc(aux.getTitulo(), this.descricao, (String) session.get("username"));
                WebSocketAnnotation.notifyEditors(aux.getTitulo(), aux.getUsers());
            }catch(RemoteException e){
                e.printStackTrace();
                return ERROR;
            }
            return SUCCESS;
        } else {
            return INPUT;
        }
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
     * Getter do album atual
     *
     * @return album
     */
    public Album getAlbum(){
        return (Album) this.session.get("album_atual");
    }
}
