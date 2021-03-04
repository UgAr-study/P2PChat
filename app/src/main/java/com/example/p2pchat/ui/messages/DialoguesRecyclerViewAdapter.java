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

public class DialoguesRecyclerViewAdapter extends RecyclerView.Adapter<DialoguesRecyclerViewAdapter.DialogueViewHolder> {

    ArrayList<DialogueItem> dialogueItems;
    Context context;

    public DialoguesRecyclerViewAdapter (Context ct) {
        context = ct;
        dialogueItems = new ArrayList<>();
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

    public class DialogueViewHolder extends RecyclerView.ViewHolder {
        TextView Name;
        TextView timeStamp;
        TextView message;

        public DialogueViewHolder(@NonNull View itemView) {
            super(itemView);
            Name  = itemView.findViewById(R.id.nameOfUser);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            message   = itemView.findViewById(R.id.last_messageText);
        }
    }
}