package com.example.p2pchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.p2pchat.dataTools.SQLUserData;
import com.example.p2pchat.dataTools.SQLUserInfo;
import com.example.p2pchat.network.MessageInfo;
import com.example.p2pchat.ui.chat.MessageItem;
import com.example.p2pchat.ui.chat.ChatRecyclerViewAdapter;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChatActivity extends AppCompatActivity {
    static private ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    static private ArrayList<MessageItem> mMessages;
    static private SQLUserData sqlUserData;
    static private final int NUM_LOAD_ROWS = 50;
    static private String userPubKey;
    static private String userName;
    static private String tableUserInfo;
    static private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        userPubKey = intent.getStringExtra(MainActivity.EXTRA_USER_PUBLIC_KEY);
        userName = intent.getStringExtra(MainActivity.EXTRA_USER_NAME);
        userId = intent.getStringExtra(MainActivity.EXTRA_USER_ID);
        tableUserInfo = intent.getStringExtra(MainActivity.EXTRA_USER_INFO_TABLE);

        SQLUserInfo dbUI = new SQLUserInfo(this, tableUserInfo);
        sqlUserData = new SQLUserData(getBaseContext(),
                dbUI.getIdByPublicKey(userPubKey).get(0));

        mMessages = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, mMessages);
        recyclerView.setAdapter(chatRecyclerViewAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);

        loadChat(NUM_LOAD_ROWS);
    }

    @Override
    protected void onDestroy() {
        sqlUserData.close();
        super.onDestroy();
    }

    public String getUserId() {return userId;}

    public void onClickSendButton (View v) {
        EditText editText = findViewById(R.id.message);
        String message = editText.getText().toString();
        editText.setText(null);
        if (message.trim().isEmpty()) {
            Toast.makeText(this, "Empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar currentTime = new GregorianCalendar(TimeZone.getDefault());
        chatRecyclerViewAdapter.addItem(new MessageItem(userName, message, currentTime));
        sqlUserData.insert(userName , currentTime, message);
        Log.d("myLogsChatActivity", "Msg saved");
    }

    static public void loadChat(int numRows) {
        Observable<ArrayList<MessageItem>> observable = Observable.create(emmit -> {
            try {
                ArrayList<MessageItem> msgs = ChatActivity.sqlUserData.loadLastMsg(numRows);
                emmit.onNext(msgs);
            } catch (Exception e) {
                emmit.onError(e);
            }

            emmit.onComplete();
        });

        observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<MessageItem>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        //nothing
                    }

                    @Override
                    public void onNext(@NonNull ArrayList<MessageItem> msgs) {
                        for (MessageItem it : msgs) {
                            ChatActivity.chatRecyclerViewAdapter.addItem(it);
                        }
                        ChatActivity.mMessages.addAll(msgs);
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
    }

    static public void loadNewMsg(MessageInfo msg) {
        if (msg.getId().equals(ChatActivity.userId)) {
            ChatActivity.chatRecyclerViewAdapter.addItem(msg.getMessageItem());
        }
    }
}