package models;

import java.io.Serializable;

public class Critica implements Serializable {
    static int maxLength = 300;
    int pontuacao;
    String justificacao;

    /**
     * Construtor por parâmetros
     *
     * @param pontuacao    int Pontuação da critica
     * @param justificacao String justificação da critica
     * @throws ExceedsMaxLengthException Critica excede os 300 chars estabelecidos
     */
    public Critica(int pontuacao, String justificacao) throws ExceedsMaxLengthException {
        if (justificacao.length() > Critica.maxLength) {
            throw new ExceedsMaxLengthException("Critique exceeds 300 chars");
        } else {
            this.justificacao = justificacao;
            this.pontuacao = pontuacao;
        }
    }

    /**
     * Getter - Pontuação
     *
     * @return int pontuação da critica
     */

    public int getPontuacao() {
        return pontuacao;
    }

    /**
     * Getter - Justificação
     * @return String justificação da critica
     */
    public String getJustificacao() {
        return justificacao;
    }
}