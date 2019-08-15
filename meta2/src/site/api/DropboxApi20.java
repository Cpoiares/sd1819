package site.api;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.Verb;

/**
 * Classe auxiliar que define o api do dropbox usando DefaultApi20
 */
public class DropboxApi20 extends DefaultApi20 {

    public final static DropboxApi20 INSTANCE = new DropboxApi20();

    /**
     * Devolve default getAccessTokenEndpoint
     *
     * @return url para obter o access_token dando um authorization code.
     */
    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.dropboxapi.com/oauth2/token";
    }

    /**
     * Devolve o url base para obter authorization code
     *
     * @return url
     */
    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://www.dropbox.com/oauth2/authorize";
    }

    /**
     * Default verb do cliente rest
     *
     * @return Verb.POST
     */
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
}