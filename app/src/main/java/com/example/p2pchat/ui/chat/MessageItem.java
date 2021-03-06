package com.example.p2pchat.ui.chat;

public class MessageItem {
    private String name;
    private int time;
    private String message;

    public MessageItem(String name, String msg, int time) {
        this.name = name;
        this.message = msg;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public int getTime() {
        return time;
    }
}
