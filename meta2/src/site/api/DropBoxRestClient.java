package site.api;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Cliente rest do DropBox
 */
public class DropBoxRestClient {
    private static final String API_APP_KEY = "juzih9ecafrb4ro";
    private static final String API_APP_SECRET = "xad4qmynv0aznjs";
    private static OAuth20Service service = new ServiceBuilder(API_APP_KEY).apiSecret(API_APP_SECRET).build(DropboxApi20.INSTANCE);

    /**
     * Metodo para obter o url temporario de um ficheiro
     *
     * @param accessToken access_token
     * @param filePath    filePath or fileId
     * @return response
     */
    public static Response getTemporaryLink(OAuth2AccessToken accessToken, String filePath) {
        Response response = null;
        OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.dropboxapi.com/2/files/get_temporary_link");

        JSONObject payload = new JSONObject();
        payload.put("path", filePath);

        request.addHeader("Content-Type", "application/json");
        request.setPayload(payload.toString());
        try {
            service.signRequest(accessToken, request);
            response = service.execute(request);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Metodo para adicionar varios users (lista de ids) a um ficheiro
     *
     * @param accessToken access_token
     * @param filePath    filePath or id
     * @param ids      lista de ids
     * @return response
     */
    public static Response addFileMember(OAuth2AccessToken accessToken, String filePath, ArrayList<String> ids) {
        Response response = null;
        OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.dropboxapi.com/2/sharing/add_file_member");

        JSONObject payload = new JSONObject();
        payload.put("file", filePath);

        JSONArray members = new JSONArray();
        for (String id : ids) {
            JSONObject member = new JSONObject();
            member.put("dropbox_id", id);
            member.put(".tag", "dropbox_id");

            System.out.println(members);
            System.out.println(member);
            members.put(member);
        }

        payload.put("custom_message", "dropmusic");
        payload.put("add_message_as_comment", true);
        payload.put("members", members);
        System.out.println("PAYLOAD");
        System.out.println(payload);
        System.out.println("-------");

        request.addHeader("Content-Type", "application/json");
        request.setPayload(payload.toString());
        try {
            service.signRequest(accessToken, request);
            response = service.execute(request);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Metodo para obter informacao da conta atual
     *
     * @param accessToken access_token
     * @return response
     */
    public static Response getCurrentAccount(OAuth2AccessToken accessToken) {
        Response response = null;
        OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.dropboxapi.com/2/users/get_current_account");
        request.addHeader("Content-Type", "application/json");
        request.setPayload("null");
        try {
            service.signRequest(accessToken, request);
            response = service.execute(request);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Metodo proxy para chamar com access_token em string
     *
     * @param accessToken access_token
     * @param filePath    filePath or id
     * @return response
     */
    public static Response getTemporaryLink(String accessToken, String filePath) {
        return getTemporaryLink(new OAuth2AccessToken(accessToken), filePath);
    }

    /**
     * Metodo proxy para chamar com access_token em string
     *
     * @param accessToken access_token
     * @param filePath    filePath or id
     * @param emails      lista de emails
     * @return response
     */
    public static Response addFileMember(String accessToken, String filePath, ArrayList<String> emails) {
        return addFileMember(new OAuth2AccessToken(accessToken), filePath, emails);
    }

    /**
     * Metodo proxy para chamar com access_token em string
     *
     * @param accessToken access_token
     * @return response
     */
    public static Response getCurrentAccount(String accessToken) {
        return getCurrentAccount(new OAuth2AccessToken(accessToken));
    }
}
