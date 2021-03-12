package com.example.p2pchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.p2pchat.dataTools.SQLUserData;
import com.example.p2pchat.dataTools.SQLUserInfo;
import com.example.p2pchat.network.MessageInfo;
import com.example.p2pchat.network.MessageObject;
import com.example.p2pchat.network.TCPSender;
import com.example.p2pchat.ui.chat.MessageItem;
import com.example.p2pchat.ui.chat.ChatRecyclerViewAdapter;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    static private String recipientPubKey;
    static private String recipientName;
    static private String tableUserInfo;
    static private String recipientId;
    static private String myPubKey;
    static private String aesKey;

    static public final String EXTRA_RECIPIENT_PUBKEY = "recipient public key";
    static public final String EXTRA_SENDER_PUBKEY = "sender public key";
    static public final String EXTRA_RECIPIENT_NAME = "recipient name";
    static public final String EXTRA_RECIPIENT_ID = "recipient id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        recipientPubKey = intent.getStringExtra(MainActivity.EXTRA_USER_PUBLIC_KEY);
        recipientName = intent.getStringExtra(MainActivity.EXTRA_USER_NAME);
        recipientId = intent.getStringExtra(MainActivity.EXTRA_USER_ID);
        tableUserInfo = intent.getStringExtra(MainActivity.EXTRA_USER_INFO_TABLE);
        myPubKey = null;
        aesKey = null;

        SQLUserInfo dbUI = new SQLUserInfo(this, tableUserInfo);
        sqlUserData = new SQLUserData(getBaseContext(),
                dbUI.getIdByPublicKey(recipientPubKey).get(0));

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

    public String getUserId() {return recipientId;}

    public void onClickSendButton (View v) {
        EditText editText = findViewById(R.id.message);
        String message = editText.getText().toString();
        editText.setText(null);
        if (message.trim().isEmpty()) {
            Toast.makeText(this, "Empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MessageObject messageObject = new MessageObject(recipientPubKey, myPubKey, message, aesKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException) {

        }
        TCPSender tcpSender = new TCPSender(messageObject, aesKey);


        Calendar currentTime = new GregorianCalendar(TimeZone.getDefault());
        chatRecyclerViewAdapter.addItem(new MessageItem(recipientName, message, currentTime));
        sqlUserData.insert(recipientName, currentTime, message);
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
        if (msg.getId().equals(ChatActivity.recipientId)) {
            ChatActivity.mMessages.add(msg.getMessageItem());
            ChatActivity.chatRecyclerViewAdapter.addItem(msg.getMessageItem());
        }
    }
}