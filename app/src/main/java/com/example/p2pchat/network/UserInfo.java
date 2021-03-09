package com.example.p2pchat.network;

public class UserInfo {

    private String name;
    private String publicKey;
    private String ipAddress;
    private boolean isNew;

    public UserInfo (String name, String publicKey, String ipAddress) {
        this.name      = name;
        this.publicKey = publicKey;
        this.ipAddress = ipAddress;
        isNew = false;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isNewUser() {
        return isNew;
    }

    public void setIsNewStatus(boolean status) {
        isNew = status;
    }
}
