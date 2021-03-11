package com.example.p2pchat.network;

import android.util.Log;

import com.example.p2pchat.ui.chat.MessageItem;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessageInfo {

    private MessageItem messageItem;
    private String id;
    private String publicKey;

    public MessageInfo (MessageItem item, String userId, String userPublicKey) {
        messageItem = item;
        id = userId;
        publicKey = userPublicKey;
    }

    public MessageItem getMessageItem() {
        return messageItem;
    }

    public String getId() {
        return id;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
