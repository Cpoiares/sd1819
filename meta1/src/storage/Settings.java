package storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe auxiliar para fazer a leitura do ficheiro de confirgurações.
 * <p>
 * Configuração exemplo
 * <p>
 * multicast_port=4321
 * registry_url=rmi://localhost/
 * rmi_timeout=2000
 * socket_timeout=31000
 * rmi_retries=5
 */
public class Settings {
    private int multicastPort;
    private String registryUrl;
    private String multicastAddress;
    private int rmiTimeout;
    private int socketTimeout;
    private int rmiRetries;

    /**
     * Construtor da classe Settings
     * O ficheiro de definições tem que ser './config.properties'
     */
    public Settings() {
        String fileName = "config.properties";
        String defaultMulticastAddress = "224.0.223.0";
        String defaultMulticastPort = "4321";
        String defaultRegistryUrl = "rmi://localhost/";
        String defaultRmiTimeout = "2000";
        String defaultSocketTimeout = "10000";
        String defaultRmiRetries = "5";

        try {
            Properties props = new Properties();
            InputStream input = new FileInputStream(fileName);
            props.load(input);

            multicastAddress = props.getProperty("multicast_address", defaultMulticastAddress);
            multicastPort = Integer.parseInt(props.getProperty("multicast_port", defaultMulticastPort));
            registryUrl = props.getProperty("registry_url", defaultRegistryUrl);
            rmiTimeout = Integer.parseInt(props.getProperty("rmi_timeout", defaultRmiTimeout));
            socketTimeout = Integer.parseInt(props.getProperty("socket_timeout", defaultSocketTimeout));
            rmiRetries = Integer.parseInt(props.getProperty("rmi_retries", defaultRmiRetries));


            input.close();

        } catch (IOException e) {
            System.out.println("Erro a ler ficheiro de definiçoes, a carregar valores por defeito...");
            multicastAddress = defaultMulticastAddress;
            multicastPort = Integer.parseInt(defaultMulticastPort);
            registryUrl = defaultRegistryUrl;
            rmiTimeout = Integer.parseInt(defaultRmiTimeout);
            socketTimeout = Integer.parseInt(defaultSocketTimeout);
        }
    }

    /**
     * get rmi_retries
     *
     * @return rmi_retries
     */
    public int getRmiRetries() {
        return rmiRetries;
    }

    /**
     * get socket_timeout
     *
     * @return socket_timeout
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * get rmi_timeout
     *
     * @return rmi_timeout
     */
    public int getRmiTimeout() {
        return rmiTimeout;
    }

    /**
     * get multicast_address
     *
     * @return multicast_address
     */
    public String getMulticastAddress() {
        return multicastAddress;
    }

    /**
     * get multicast_port
     *
     * @return multicast_port
     */
    public int getMulticastPort() {
        return multicastPort;
    }

    /**
     * get registry_url
     *
     * @return registry_url
     */
    public String getRegistryUrl() {
        return registryUrl;
    }
}
