package models;

import utils.Passwords;

import java.io.Serializable;

public class Utilizador implements Serializable {

    String username;
    boolean isEditor;
    boolean isAdmin;
    String account_id = " ";
    String access_token = " ";
    private byte[] salt;
    private byte[] passwordHash;
    /**
     * Construtor por parâmetros
     *
     * @param username String nome de utilizador
     * @param password String password
     */
    public Utilizador(String username, String password) {
        this.username = username;
        this.salt = Passwords.getNextSalt();
        this.passwordHash = Passwords.hash(password.toCharArray(), this.salt);
        isEditor = false;
        isAdmin = false;
    }
    /**
     * Construtor por parâmetros
     *
     * @param username String nome de utilizador
     * @param isEditor boolean permissões de editor
     * @param isAdmin  boolean permissões de admin
     */
    public Utilizador(String username, boolean isEditor, boolean isAdmin) {
        this.username = username;
        this.isEditor = isEditor;
        this.isAdmin = isAdmin;
    }
    /**
     * Construtor por parâmetros
     *
     * @param username String nome de utilizador
     */
    public Utilizador(String username) {
        this.username = username;
    }

    /**
     * Getter para o access_token da dropbox do utilizador
     *
     * @return access_token
     */
    public String getAccess_token() {
        return access_token;
    }

    /**
     * Setter para o access_token da dropbox de um utilizador
     *
     * @param access_token novo access_token
     */
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    /**
     * Getter para o account_id da dropbox do utilizador
     *
     * @return account_id
     */
    public String getAccount_id() {
        return account_id;
    }

    /**
     * Setter para o account_id da dropbox de um utilizador
     *
     * @param account_id account_id
     */
    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    /**
     * Método para confirmar a password.
     *
     * @param password String
     * @return boolean confirmação
     */
    public boolean checkPassword(String password) {
        return Passwords.isExpectedPassword(password.toCharArray(), this.salt, this.passwordHash);
    }

    /**
     * Getter - isEditor
     *
     * @return boolean permissões de editor
     */
    public boolean getIsEditor() {
        return isEditor;
    }

    /**
     * Setter - isEditor
     *
     * @param isEditor boolean permissões de editor
     */
    public void setIsEditor(boolean isEditor) {
        this.isEditor = isEditor;
    }

    /**
     * Getter - isAdmin
     *
     * @return boolean permissões de administrador
     */
    public boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * Setter - isAdmin
     *
     * @param isAdmin boolean permissões de administrador
     */
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Getter - username
     *
     * @return String nome de utilizador
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Método de comparação de strings
     *
     * @param username String nome do utilizador
     * @return boolean igualdade
     */
    public boolean equals(String username) {
        return this.username.equals(username);
    }

    /**
     * Método de conversão do Utilizador para String
     *
     * @return String com informação do utilizador
     */
    @Override
    public String toString() {
        return String.format("<%s, admin:%s, editor:%s>", username, isAdmin, isEditor);
    }
}
