package site.ws;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.Session;

/**
 * Classe de WebSocket Server.
 */

@ServerEndpoint(value = "/ws")
public class WebSocketAnnotation {
    private static final AtomicInteger sequence = new AtomicInteger(1);
    private static HashMap<String, WebSocketAnnotation> clients = new HashMap<>();
    private String username;
    private Session session;
    private boolean firstMessage;

    /**
     * Construtor por omissão.
     */
    public WebSocketAnnotation(){
        this.username = "User " + sequence.getAndIncrement();
        this.firstMessage = true;
    }

    /**
     * Método para o cliente da alteração das permissões de editor.
     * Procura no hashmap de clientes, se o cliente estiver conectado a uma WebSocket
     * envia mensagem do tipo "MAKE_EDITOR".
     * @param username username
     */
    public static void make_editor(String username) {
        if (clients.containsKey(username)) {
            JSONObject json = new JSONObject();
            json.put("type", "MAKE_EDITOR");
            clients.get(username).sendMessage(json.toString());
        } else {
            System.out.println("User " + username + " is not online atm.");
        }
    }

    /**
     * Método para notificar um cliente de uma pontuação média do album.
     * Procura no hashmap de clientes, se o cliente estiver conectado a uma WebSocket envia
     * mensagem do tipo "average" com a nova pontuação, album e artista.
     * @param artista
     * @param album
     * @param pontuacao
     */
    public static void average(String artista, String album, float pontuacao) {
        for (WebSocketAnnotation manito : clients.values()) {
            JSONObject json = new JSONObject();
            json.put("album", album);
            json.put("artista", artista);
            json.put("pontuacao", pontuacao);
            json.put("type", "average");
            System.out.println(json);
            manito.sendMessage(json.toString());
        }
    }

    /**
     * Método para notificar os editores anteriores da re-edição da descrição de um album.
     * Procura no hashmap de clientes, se o cliente estiver conectado a um WebSocket envia
     * mensagem do tipo "album_desc" e o titulo do album alterado.
     * @param titulo
     * @param previousEditors
     */
    public static void notifyEditors(String titulo, ArrayList<String> previousEditors) {
        JSONObject json = new JSONObject();
        json.put("type", "album_desc");
        json.put("titulo", titulo);
        for (String i : previousEditors) {
            if (clients.containsKey(i)) {
                clients.get(i).sendMessage(json.toString());
            }
        }
    }

    /**
     * Método para iniciar uma WebSocket.
     * Guarda a sessão do cliente.
     * @param session
     */
    @OnOpen
    public void start(Session session){
        this.session = session;
    }

    /**
     * Método para fechar uma WebSocket.
     * Remove o username do cliente do hashmap de clientes.
     */
    @OnClose
    public void end(){
        // clean up
        clients.remove(username);
    }

    /**
     * Método para receber uma mensagem.
     * Caso seja a primeira mensagem recebida por um dado cliente, recebe a mensagem com
     * o username e adiciona o username recebido ao hashmap de clientes.
     * @param message
     */
    @OnMessage
    public void receiveMessage(String message) {
        if (firstMessage) {
            System.out.println("First message from " + message);
            this.username = message;
            clients.put(message, this);
            this.firstMessage = false;
            System.out.println(this.username);
        }
    }

    /**
     * Método para o tratamento de erros.
     * @param t
     */
    @OnError
    public void handleError(Throwable t){
        t.printStackTrace();
    }

    /**
     * Método para o envio de mensagem.
     * @param text
     */
    private void sendMessage(String text) {
        System.out.println("sendMessage(): " + text);
        // uses *this* object's session to call sendText()
        try {
            this.session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            // clean up once the WebSocket connection is closed
            try {
                this.session.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}

