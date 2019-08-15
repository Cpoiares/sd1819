package site.action;

import com.opensymphony.xwork2.ActionSupport;
import models.Album;
import models.Artista;
import models.Musica;
import org.apache.struts2.interceptor.SessionAware;

import org.json.JSONObject;
import site.api.DropBoxRestClient;
import site.model.UserBean;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Classe de acoes relacionadas com musicas
 */
public class DetalhesMusicaAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private Musica musica;
    private String titulo;
    private String albumNome;
    private String artistaNome;
    private String id;
    private String tempLink;
    private String account_id;

    /**
     * Acao de listagem de detalhes de musica
     *
     * @return resultado da acao
     */
    @Override
    public String execute() {
        try {
            ArrayList<Artista> artistas = this.getUserBean().getArtistas();
            for(Artista artista : artistas) {
                if (artista.equals(this.artistaNome)) {
                    for(Album album : artista.getAlbuns()) {
                        if(album.equals(this.albumNome)) {
                            for(Musica musica : album.getMusicas()) {
                                if (musica.equals(this.titulo)) {
                                    this.musica = musica;
                                    session.put("musica_atual", musica);
                                    String response = null;
                                    try {
                                        response = DropBoxRestClient.getTemporaryLink((String) session.get("access_token"), musica.getFilePath()).getBody();
                                        System.out.println(response);
                                        if (response != null) {
                                            JSONObject json = new JSONObject(response);
                                            this.tempLink = json.getString("link");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        this.tempLink = null;
                                    }
                                    return SUCCESS;
                                }
                            }
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
     * Acao de adicionar um file_id da dropbox a uma música
     *
     * @return resultado da acao
     */
    public String adicionarMusicaDropbox() {
        this.getUserBean().setSongId(titulo, albumNome, artistaNome, id);
        return SUCCESS;
    }

    /**
     * Acao de adicionar account_id a um ficheiro da dropbox
     *
     * @return resultado da acao
     */
    public String addFileMembers() {
        this.musica = this.getUserBean().getMusica(this.artistaNome, this.albumNome, this.titulo);
        ArrayList<String> emails = new ArrayList<>();
        emails.add(this.account_id);
        String response = null;
        try {
            response = DropBoxRestClient.addFileMember((String) session.get("access_token"), musica.getFilePath(), emails).getBody();
        } catch (IOException e) {
            e.printStackTrace();
            return ERROR;
        }
        System.out.println(response);
        return SUCCESS;
    }

    /**
     * Getter do account_id com quem partilhar o ficheiro da musica
     *
     * @return account_id
     */
    public String getAccount_id() {
        return account_id;
    }

    /**
     * Setter do account_id com quem partilhar o ficheiro da musica
     *
     * @param account_id account_id
     */
    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    /**
     * Getter do link temporario de um ficheiro para tocar no browser diretamente
     *
     * @return link temporario
     */
    public String getTempLink() {
        return tempLink;
    }

    /**
     * Setter do link temporario de um ficheiro para tocar no browser diretamente
     *
     * @param tempLink link temporario
     */
    public void setTempLink(String tempLink) {
        this.tempLink = tempLink;
    }

    /**
     * Getter do id do ficheiro de musica no dropbox
     *
     * @return id do ficheiro
     */
    public String getId() {
        return id;
    }

    /**
     * Setter do id do ficheiro a adicionar à musica
     *
     * @param id file_id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter da musica atual
     *
     * @return musica
     */
    public Musica getMusica() {
        return musica;
    }

    /**
     * Setter da musica atual
     *
     * @param musica musica
     */
    public void setMusica(Musica musica) {
        this.musica = musica;
    }

    /**
     * Getter do titulo da musica
     *
     * @return titulo da musica
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Setter do titulo da musica
     *
     * @param titulo titulo da musica
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Getter o nome do album da musica
     *
     * @return nome do album
     */
    public String getAlbumNome() {
        return albumNome;
    }

    /**
     * Setter do nome do album da musica
     *
     * @param albumNome nome do album
     */
    public void setAlbumNome(String albumNome) {
        this.albumNome = albumNome;
    }

    /**
     * Getter do nome do artista
     *
     * @return nome do artista
     */
    public String getArtistaNome() {
        return artistaNome;
    }

    /**
     * Setter do nome do artista
     *
     * @param artistaNome nome do artista
     */
    public void setArtistaNome(String artistaNome) {
        this.artistaNome = artistaNome;
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
