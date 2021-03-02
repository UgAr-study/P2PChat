package com.example.p2pchat.ui.messages;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.example.p2pchat.R;

public class UsersRecyclerViewAdapter extends RecyclerView.Adapter<UsersRecyclerViewAdapter.MyViewHolder> {

    ArrayList<MessageItem> messageItems;
    Context context;

    public UsersRecyclerViewAdapter (Context ct, ArrayList<MessageItem> items) {
        context = ct;
        messageItems = items;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_users, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String name = messageItems.get(position).getName();
        SpannableString ss=new SpannableString(name);
        ss.setSpan(new UnderlineSpan(), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.fromName.setText(ss);
        holder.message.setText(messageItems.get(position).getMessage());
        holder.timeStamp.setText(messageItems.get(position).getTime());

    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    public void addItem (MessageItem item ) {
        messageItems.add(item);
        notifyItemChanged(getItemCount());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView fromName;
        TextView timeStamp;
        TextView message;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fromName  = itemView.findViewById(R.id.nameOfUser);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            message   = itemView.findViewById(R.id.last_messageText);
        }
    }
}


class MessageItem {
    private String name;
    private String time;
    private String lastMessage;

    public MessageItem(String na, String msg, String t) {
        name = na;
        lastMessage = msg;
        time = t;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return lastMessage;
    }

    public String getTime() {
        return time;
    }

}