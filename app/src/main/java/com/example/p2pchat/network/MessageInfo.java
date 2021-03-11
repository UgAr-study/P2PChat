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

/*
    public void onUpdate() {
        Observable<String> observable = Observable.create(emmit -> {
            try {
                String msg = UserDataTable.loadLastMsg();
                emmit.onNext(msg);
            } catch (Exception e) {
                emmit.onError(e);
            }

            emmit.onComplete();
        });

        observable
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        //nothing
                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        RecyclerViewAdapter.addItem(s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("MyTag", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("MyTag", "updater has completed");
                    }
                });
    }*/
}
