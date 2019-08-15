package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Album;
import models.Artista;
import models.Critica;
import org.apache.struts2.interceptor.SessionAware;
import site.model.UserBean;
import site.ws.WebSocketAnnotation;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Acao para criticar um album
 */
public class CriticarAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String justificacao;
    private int pontuacao;

    /**
     * Acao para criticar um album.
     *
     * @return resultado da acao
     */
    @Override
    public String execute() {
        if(this.justificacao != null && this.pontuacao != 0) {
            try{
                ArrayList<Artista> artistas = this.getUserBean().getArtistas();
                Album aux = (Album) session.get("album_atual");
                float average = 0;
                for(Artista artist : artistas){
                    if(artist.getNome().equals(aux.getAutor().getNome())){
                        for(Album album : artist.getAlbuns()){
                            if(album.getTitulo().equals(aux.getTitulo())){
                                this.getUserBean().addCritique(album.getTitulo(), Integer.toString(this.pontuacao), this.justificacao);
                                for(Critica i: album.getCriticas()){
                                    average += i.getPontuacao();
                                }
                                average = average + this.pontuacao;
                                average = average/(album.getCriticas().size() + 1);
                                WebSocketAnnotation.average(aux.getAutor().getNome(), aux.getTitulo(), average);
                                break;
                            }
                        }
                    }
                }
            }catch (RemoteException e)  {
                e.printStackTrace();
                return ERROR;
            }
            System.out.println("Sucesso");
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
     * Setter da justificaçao da nova critica
     *
     * @param justificacao text de justificacao
     */
    public void setJustificacao(String justificacao) { this.justificacao = justificacao; }

    /**
     * Setter da pontuacao da nova critica
     *
     * @param pontuacao pontuacao da nova critica
     */
    public void setPontuacao(int pontuacao){ this.pontuacao = pontuacao; }
}
