package com.example.p2pchat.ui.chat;

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

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.MyViewHolder> {

    ArrayList<MessageItem> messageItems;
    Context context;

    public ChatRecyclerViewAdapter(Context ct) {
        context = ct;
        messageItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_message, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String name = messageItems.get(position).getName();
        SpannableString ss = new SpannableString(name);
        ss.setSpan(new UnderlineSpan(), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.fromName.setText(ss);
        holder.message.setText(messageItems.get(position).getMessage());
        holder.timeStamp.setText(messageItems.get(position).getTimeHoursMinutes());
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    public void addItem (MessageItem item) {
        messageItems.add(item);
        notifyItemChanged(getItemCount());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView fromName;
        TextView timeStamp;
        TextView message;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fromName  = itemView.findViewById(R.id.nameOfSender);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            message   = itemView.findViewById(R.id.messageText);
        }
    }
}
