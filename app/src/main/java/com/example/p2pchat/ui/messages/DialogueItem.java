package com.example.p2pchat.ui.messages;

public class DialogueItem {

    private String userName;
    private String lastTime; //TODO: may be change to Calendar?
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
        lastMessage = new String("No messages yet");
        lastTime = time;
        userPublicKey = "No public key";
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

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
