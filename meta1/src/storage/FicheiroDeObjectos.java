package storage;
import java.io.*;

public class FicheiroDeObjectos {
    private ObjectInputStream iS;
    private ObjectOutputStream oS;

    /**
     * abre um ficheiro para leitura de objectos
     * @param  nomeDoFicheiro nome do Ficheiro
     * @throws IOException exception
     */
    public void abreLeitura(String nomeDoFicheiro) throws IOException {
        iS = new ObjectInputStream(new FileInputStream(nomeDoFicheiro));
    }
    /**
     * abre um ficheiro para escrita de objectos
     * @param  nomeDoFicheiro nome do Ficheiro
     * @throws IOException exception
     */
    public void abreEscrita(String nomeDoFicheiro) throws IOException {
        oS = new ObjectOutputStream(new FileOutputStream(nomeDoFicheiro));
    }

    /**
     * escreve um objecto em ficheiro
     * @param  o             objecto a escrever
     * @throws IOException exception
     */
    public void escreveObjecto(Object o) throws IOException {
        oS.writeObject(o);
    }

    /**
     * le um objecto de um ficheiro
     * @return objecto lido
     * @throws IOException            exception
     * @throws ClassNotFoundException exception
     */
    public Object leObjecto() throws IOException, ClassNotFoundException {
        try{
            return iS.readObject();
        } catch(EOFException | ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * fecha um ficheiro para a leitura
     * @throws IOException exception
     */
    public void fechaLeitura() throws IOException {
        iS.close();
    }

    /**
     * fecha um ficheiro para escrita
     * @throws IOException [description]
     */
    public void fechaEscrita() throws IOException {
        oS.close();
    }
}