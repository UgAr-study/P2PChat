package com.example.p2pchat.ui.messages;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p2pchat.R;
import com.example.p2pchat.network.MessageInfo;

import java.util.ArrayList;

public class DialoguesFragment extends Fragment {

    private RecyclerView dialogueRecyclerView;
    private DialoguesRecyclerViewAdapter dialogueAdapter;
    private LinearLayoutManager lm;
    //private Context context;
    private final String SAVED_STATUS_ID = "saved_status_id";
    private final String SAVED_DATASET = "seved_dataset";

    public static DialoguesFragment getDialoguesFragment(Context c, DialoguesRecyclerViewAdapter.OnItemClickListener listener) {
        DialoguesFragment f = new DialoguesFragment();
        //f.context = c;
        f.dialogueAdapter = new DialoguesRecyclerViewAdapter(c, listener);
        Log.d("DialogFragment", "\"constructor\"");
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d("DialogFragment", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_dialogues, container, false);

        dialogueRecyclerView = view.findViewById(R.id.dialogue_recyclerView);
        lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(false);

        dialogueRecyclerView.setAdapter(dialogueAdapter);
        dialogueRecyclerView.setLayoutManager(lm);
        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("DialogFragment", "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DialogFragment", "onCreate");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("DialogFragment", "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DialogFragment", "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("DialogFragment", "onDetach");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("DialogFragment", "onResume");
    }

    public void onUpdateDialoguesList (DialogueItem dItem) {

        if (dItem == null) {
            Log.e("DialogFragment", "onUpdateDialoguesList: dItem is null");
            Toast.makeText(getContext(), "Fail: try to add null object", Toast.LENGTH_SHORT).show();
            return;
        }

        dialogueAdapter.addItem(dItem);
        Log.d("DialogFragment", "onUpdateDialoguesList: item added");
    }

    public void onUpdateLastMessage(MessageInfo messageInfo) {

        if (messageInfo == null) {
            Log.e("DialogFragment", "onUpdateLastMessage: messageInfo is null");
            return;
        }

        dialogueAdapter.setLastMessage(messageInfo);
        Log.d("DialogFragment", "onUpdateLastMessage: messageInfo added");
    }
}