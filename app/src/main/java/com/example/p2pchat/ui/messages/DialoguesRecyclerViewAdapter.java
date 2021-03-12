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
import com.example.p2pchat.network.MessageInfo;

public class DialoguesRecyclerViewAdapter extends RecyclerView.Adapter<DialoguesRecyclerViewAdapter.DialogueViewHolder> {

    ArrayList<DialogueItem> dialogueItems;
    Context context;
    OnItemClickListener listener;

    public DialoguesRecyclerViewAdapter (Context ct, OnItemClickListener itemClickListener) {
        context = ct;
        dialogueItems = new ArrayList<>();
        listener = itemClickListener;
    }

    @NonNull
    @Override
    public DialogueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_users, parent, false);
        return new DialogueViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogueViewHolder holder, int position) {

        String name = dialogueItems.get(position).getName();
        SpannableString ss = new SpannableString(name);
        ss.setSpan(new UnderlineSpan(), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.Name.setText(ss);
        holder.message.setText(dialogueItems.get(position).getMessage());
        holder.timeStamp.setText(dialogueItems.get(position).getTime());

    }

    @Override
    public int getItemCount() {
        return dialogueItems.size();
    }

    public void addItem (DialogueItem item) {
        dialogueItems.add(item);
        notifyItemChanged(getItemCount());
    }

    public void setLastMessage (MessageInfo messageInfo) {
        int i = findByPublicKey(messageInfo.getPublicKey());
        if (i == -1)
            return;

        dialogueItems.get(i).setLastMessage(messageInfo.getMessageItem().getMessage());
        dialogueItems.get(i).setLastTime(messageInfo.getMessageItem().getTimeHoursMinutes());
        notifyItemChanged(i);
    }

    public class DialogueViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView Name;
        TextView timeStamp;
        TextView message;

        public DialogueViewHolder(@NonNull View itemView) {
            super(itemView);
            Name  = itemView.findViewById(R.id.nameOfUser);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            message   = itemView.findViewById(R.id.last_messageText);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(dialogueItems.get(this.getLayoutPosition()));
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(DialogueItem item);
    }

    private int findByPublicKey(String publicKey) {

        for (int i = 0, end = dialogueItems.size(); i != end; ++i) {
            if (dialogueItems.get(i).getUserPublicKey().equals(publicKey))
                return i;
        }

        return -1;
    }
}