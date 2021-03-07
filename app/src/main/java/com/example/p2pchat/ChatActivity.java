package com.example.p2pchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.p2pchat.dataTools.SQLUserData;
import com.example.p2pchat.ui.chat.MessageItem;
import com.example.p2pchat.ui.chat.RecyclerViewAdapter;
import com.example.p2pchat.ui.chat.RecyclerViewAdapter;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ChatActivity extends AppCompatActivity {
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<MessageItem> mMessages;
    private SQLUserData sqlUserData;
    static private final int NUM_LOAD_ROWS = 50;
    private String userPubKey;
    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        userPubKey = intent.getStringExtra(MainActivity.EXTRA_USER_PUBLIC_KEY);
        userName = intent.getStringExtra(MainActivity.EXTRA_USER_NAME);

        sqlUserData = new SQLUserData(getBaseContext(), userPubKey);

        mMessages = sqlUserData.loadLastMsg(NUM_LOAD_ROWS);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(this, mMessages);
    }

    @Override
    protected void onDestroy() {
        sqlUserData.close();
        super.onDestroy();
    }

    public void onClickSendButton (View v) {
        EditText editText = findViewById(R.id.message);
        String message = editText.getText().toString();
        editText.setText(null);
        if (message.trim().isEmpty()) {
            Toast.makeText(this, "Empty message", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar currentTime = new GregorianCalendar();
        recyclerViewAdapter.addItem(new MessageItem(userName, message, currentTime));
        sqlUserData.insert(currentTime, message);
    }

}