package client;

import java.io.*;
import java.net.Socket;

/**
 * Classe do servidor TCP do cliente.
 */
class TCPClient {

    private String address;
    private int port;
    private String fileName;

    /**
     * Construtor do cliente
     *
     * @param address  string com o endereço do servidor
     * @param port     porta do servidor
     * @param filename path do ficheiro a fazer upload/download
     */
    TCPClient(String address, int port, String filename) {
        this.fileName = filename;
        this.address = address;
        this.port = port;
    }

    /**
     * Método para fazer upload de um ficheiro para o servidor
     */
    void sendFile() {
        FileInputStream fis;
        BufferedInputStream bis;
        OutputStream os;
        File file = new File(this.fileName);

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);

            Socket socket = new Socket(this.address, this.port);
            os = socket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

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

                if ((((current * 100) / fileLength) % 25) == 0) {
                    System.out.println(String.format("A enviar ficheiro ... %d%%", (current * 100) / fileLength));
                }
            }
            System.out.println(String.format("Ficheiro '%s' enviado.", fileName));
            socket.close();
        } catch (IOException e) {
            System.out.println(String.format("Erro na leitura do ficheiro '%s'. Tente outra vez.", fileName));
        }
    }

    /**
     * Método para fazer download de um ficheiro musical do servidor.
     */
    void downloadFile() {

        try {
            Socket socket = new Socket(this.address, this.port);

            InputStream is = socket.getInputStream();

            // get file extension
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String extension = dis.readUTF();

            if (extension.length() > 0) {
                fileName = String.format("%s.%s", fileName, extension);
            }

            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int bytesRead;
            byte[] contents = new byte[10000];

            System.out.println(String.format("A transferir ficheiro para '%s'", this.fileName));
            while ((bytesRead = is.read(contents)) != -1) {
                bos.write(contents, 0, bytesRead);
            }

            System.out.println("Transferência concluída.");

            bos.flush();
            socket.close();

        } catch (IOException e) {
            System.out.println(String.format("Erro na escrita do ficheiro '%s'. Tente outra vez.", fileName));
        }
    }
}
