package com.sieg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private String host;
    private String user;
    private String pass;
    private String apikey;
    private String clientid;
    private String secret;

    private int nfeCount = 0;
    private int nfceCount = 0;

    // Getters and Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getNfeCount() {
        return nfeCount;
    }

    public void setNfeCount(int nfeCount) {
        this.nfeCount = nfeCount;
    }

    public int getNfceCount() {
        return nfceCount;
    }

    public void setNfceCount(int nfceCount) {
        this.nfceCount = nfceCount;
    }

    @Override
    public String toString() {
        return "Config{host='" + host + "', user='" + user + "', clientid='" + clientid + "', nfe=" + nfeCount
                + ", nfce=" + nfceCount + "}";
    }
}
