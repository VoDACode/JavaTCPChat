package ua.nure.vovk.task3.server;

import ua.nure.vovk.task3.core.MessageCode;

public class ClientRecord {
    private String login;
    private String password;

    public ClientRecord(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!(obj instanceof ClientRecord)) {
            return false;
        }
        ClientRecord other = (ClientRecord) obj;
        return login.equals(other.login) && password.equals(other.password);
    }

    public int hashCode() {
        return login.hashCode() + password.hashCode();
    }

    public String toString() {
        return login + "=" + password;
    }

    public String getLogin() {
        return login;
    }

    public boolean isPasswordCorrect(String password) {
        return this.password.equals(password);
    }
}
