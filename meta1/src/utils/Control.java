package utils;

/**
 * Classe auxiliar para partilhar booleano entre duas threads
 * Neste projecto é utilizado para só um servidor multicast responder a um pedido
 * <p>
 * flag = true se o pedido já foi respondido
 * flag = false se ainda nao foi respondido
 */
public class Control {
    public volatile boolean flag = false;
}