package com.example.p2pchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.p2pchat.dataTools.SQLUserData;
import com.example.p2pchat.ui.chat.MessageItem;
import com.example.p2pchat.ui.chat.RecyclerViewAdapter;
import com.example.p2pchat.ui.chat.RecyclerViewAdapter;


import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<MessageItem> mMessages;
    private SQLUserData sqlUserData;
    static private final int NUM_LOAD_ROWS = 50;
    private String userPubKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        userPubKey = intent.getStringExtra(MainActivity.EXTRA_USER_PUBLIC_KEY);

        sqlUserData = new SQLUserData(getBaseContext(), userPubKey);

        mMessages = sqlUserData.loadLastMsg(NUM_LOAD_ROWS);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(this, mMessages);
    }

}