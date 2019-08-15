package server;

import models.Musica;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Classe do servidor TCP criado para fazer download/upload de ficheiros musicais.
 */
public class TCPServer extends Thread {

    private ServerSocket serverSocket;
    private String fileName;
    private Musica musica;
    private boolean isDownload;

    /**
     * Construtor do servidor para o caso de o pedido ser de upload de ficheiro.
     *
     * @param musica   musica a qual o ficheiro pertence
     * @param fileName onde guardar o ficheiro recebido
     */
    TCPServer(Musica musica, String fileName) {
        this.fileName = fileName;
        this.musica = musica;
        this.isDownload = false;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Construtor do servior para o caso do pedido ser de download de ficheiro.
     *
     * @param fileName onde o ficheiro está guardado
     */
    TCPServer(String fileName) {
        this.fileName = fileName;
        this.isDownload = true;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Socket clientSocket = serverSocket.accept();

            if (isDownload) {
                handleDownload(clientSocket);
            } else {
                handleUpload(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funcao que lida com o upload de uma musica nova.
     *
     * @param clientSocket socket do cliente
     * @throws IOException excepção da leitura de ficheiro
     */
    private void handleUpload(Socket clientSocket) throws IOException {

        // get file extension
        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
        String extension = dis.readUTF();

        if (extension.length() > 0) {
            fileName = String.format("%s.%s", fileName, extension);
        }

        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is = clientSocket.getInputStream();

        int bytesRead;
        byte[] contents = new byte[10000];

        while ((bytesRead = is.read(contents)) != -1) {
            bos.write(contents, 0, bytesRead);
        }

        bos.flush();
        clientSocket.close();
        serverSocket.close();
        musica.setFilePath(fileName);
    }

    /**
     * metodo que trata do caso de o cliente fazer download do ficheiro
     *
     * @param clientSocket socket do ciente
     */
    private void handleDownload(Socket clientSocket) {
        FileInputStream fis;
        BufferedInputStream bis;
        OutputStream os;
        DataOutputStream dos;
        File file = new File(this.fileName);

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            os = clientSocket.getOutputStream();


            dos = new DataOutputStream(clientSocket.getOutputStream());
            String[] aux = fileName.split("\\.");
            String extension = ".";
            if (aux.length > 0) {
                extension = aux[aux.length - 1];
            }
            dos.writeUTF(extension);

            byte[] contents;
            long fileLength = file.length();
            long current = 0;

            while (current != fileLength) {

                int size = 10000;
                if (fileLength - current >= size) {
                    current += size;
                } else {
                    size = (int) (fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];
                bis.read(contents, 0, size);
                os.write(contents);
            }
            System.out.println(String.format("Ficheiro '%s' enviado.", fileName));
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * metodo para desligar o servidor
     */
    void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que retorna o porto onde o servidor atual está a ouvir.
     *
     * @return porto do servidor
     */
    int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Método que retorna o endereço desta máquina na rede local
     *
     * @return string com o endereço
     */
    String getAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
