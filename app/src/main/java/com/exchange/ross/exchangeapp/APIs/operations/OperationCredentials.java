package com.exchange.ross.exchangeapp.APIs.operations;

/**
 * Created by ross on 3/21/15.
 */
public class OperationCredentials {
    private String url;
    private String domain;
    private String user;
    private String password;

    public OperationCredentials(String url, String user, String password, String domain) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.domain = domain;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {

        return url;
    }

    public String getDomain() {
        return domain;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}
