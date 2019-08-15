package site.action;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.opensymphony.xwork2.ActionSupport;
import org.json.JSONObject;
import site.api.DropBoxRestClient;
import site.api.DropboxApi20;
import site.model.UserBean;
import org.apache.struts2.interceptor.SessionAware;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LoginAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    final String id = "juzih9ecafrb4ro";
    final String secret = "xad4qmynv0aznjs";
    private Map<String, Object> session;
    private String username = null;
    private String password = null;
    private String code = null;

    /**
     * Getter do username
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter do username
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Metodo auxiliar que simplesmente direcciona o utilizador para o menu principal menu.jsp
     *
     * @return SUCCESS
     */
    public String menu() {
        return SUCCESS;
    }

    /**
     * Metodo da acao de login na dropmusic usando username e password
     *
     * @return resultado da acao
     */
    @Override
    public String execute() {
        session.clear();
        // Restrições de username ou password são verificadas no RMI
        if (this.username != null && !username.equals("")) {
            this.getUserBean().setUsername(this.username);
            this.getUserBean().setPassword(this.password);
            try {
                if (this.getUserBean().getUserMatchesPassword()) {
                    session.put("username", username);
                    session.put("loggedin", true); // this marks the user as logged in
                    session.put("editor", this.getUserBean().getUtilizador().getIsEditor());

                    if (!this.getUserBean().getUtilizador().getAccess_token().equals(" ")) {
                        session.put("access_token", this.getUserBean().getUtilizador().getAccess_token());
                    }

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
     * Metodo da acao de callback do dropbox para adicionar uma conta dropbox a uma conta DropMusic existente.
     *
     * @return resultado da acao
     */
    public String dropboxAdd() {

        OAuth2AccessToken accessToken = null;
        String callback = "http://localhost:8080/dropboxAdd";

        final OAuth20Service service = new ServiceBuilder(id).apiSecret(secret).callback(callback).build(DropboxApi20.INSTANCE);

        try {
            accessToken = service.getAccessToken(this.code);
            this.session.put("access_token", accessToken.getAccessToken());
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Response response = DropBoxRestClient.getCurrentAccount(accessToken);

        try {
            JSONObject json = new JSONObject(response.getBody());
            String account_id = json.getString("account_id");

            // adicionar à conta atual
            this.getUserBean().setAccount_id(account_id, accessToken.getAccessToken());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return SUCCESS;
    }

    /**
     * Metodo da acao de callback do dropbox para fazer login no dropmusic usando uma conta dropbox
     *
     * @return resultado da acao
     */
    public String dropboxLogin() {
        session.clear();
        OAuth2AccessToken accessToken = null;
        String callback = "http://localhost:8080/dropboxLogin";

        final OAuth20Service service = new ServiceBuilder(id).apiSecret(secret).callback(callback).build(DropboxApi20.INSTANCE);

        try {
            accessToken = service.getAccessToken(this.code);
            this.session.put("access_token", accessToken.getAccessToken());
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Response response = DropBoxRestClient.getCurrentAccount(accessToken);

        try {
            JSONObject json = new JSONObject(response.getBody());
            String account_id = json.getString("account_id");
            boolean loggedin = this.getUserBean().dropboxLogin(account_id);
            if (loggedin) {
                this.username = this.getUserBean().getUsername();
                session.put("username", username);
                session.put("loggedin", true); // this marks the user as logged in
                session.put("editor", this.getUserBean().getUtilizador().getIsEditor());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return SUCCESS;
    }

    /**
     * Metodo da acao de callback do dropbox para fazer registo no dropmusic usando somente uma conta dropbox
     *
     * @return resultado da acao
     */
    public String dropboxRegister() {
        session.clear();
        OAuth2AccessToken accessToken = null;
        String callback = "http://localhost:8080/dropboxRegister";

        final OAuth20Service service = new ServiceBuilder(id).apiSecret(secret).callback(callback).build(DropboxApi20.INSTANCE);

        try {
            accessToken = service.getAccessToken(this.code);
            this.session.put("access_token", accessToken.getAccessToken());
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Response response = DropBoxRestClient.getCurrentAccount(accessToken);

        try {
            JSONObject json = new JSONObject(response.getBody());
            String account_id = json.getString("account_id");
            username = json.getString("email");
            if (this.getUserBean().dropboxRegister(account_id, username, accessToken.getAccessToken())) {
                session.put("username", username);
                session.put("loggedin", true); // this marks the user as logged in
                session.put("editor", this.getUserBean().getUtilizador().getIsEditor());
            }
        } catch (IOException e) {
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

    /**
     * Getter do codigo de acesso dado pela dropbox (na callback)
     *
     * @param code authorization code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Setter da password do utilizador
     *
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password; // what about this input?
    }
}