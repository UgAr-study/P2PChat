package com.example.p2pchat.ui.chat;

public class MessageItem {
    private String name_;
    private int time_;
    private String message_;

    public MessageItem(String name, String msg, int time) {
        name_ = name;
        message_ = msg;
        time_ = time;
    }

    public String getName() {
        return name_;
    }

    public String getMessage() {
        return message_;
    }

    public int getTime() {
        return time_;
    }
}
