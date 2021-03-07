package com.example.p2pchat.ui.messages;

public class DialogueItem {

    private String userName;
    private String lastTime;
    private String lastMessage;
    private String userPublicKey;

    public DialogueItem (String name, String msg, String time, String publicKey) {
        userName = name;
        lastMessage = msg;
        lastTime = time;
        userPublicKey = publicKey;
    }

    public DialogueItem (String name, String msg, String time) {
        userName = name;
        lastMessage = msg;
        lastTime = time;
        userPublicKey = "No public key";
    }

    public DialogueItem (String name, String time) {
        userName = name;
        lastTime = time;
        lastMessage = new String("No messages yet");
    }

    public String getName() {
        return userName;
    }

    public String getMessage() {
        return lastMessage;
    }

    public String getTime() {
        return lastTime;
    }

    public String getUserPublicKey() {
        return userPublicKey;
    }

}
