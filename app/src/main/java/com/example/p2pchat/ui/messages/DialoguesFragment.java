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

import java.util.ArrayList;

public class DialoguesFragment extends Fragment {

    private RecyclerView dialogueRecyclerView;
    private DialoguesRecyclerViewAdapter dialogueAdapter;
    private LinearLayoutManager lm;
    //private Context context;
    private final String SAVED_STATUS_ID = "saved_status_id";
    private final String SAVED_DATASET = "seved_dataset";

    public static DialoguesFragment getDialoguesFragment(Context c) {
        DialoguesFragment f = new DialoguesFragment();
        //f.context = c;
        f.dialogueAdapter = new DialoguesRecyclerViewAdapter(c);
        Log.e("MyTAG", "DialogFragment: \"constructor\"");
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.e("MyTAG", "DialogFragment: onCreateView");
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
        Log.e("MyTAG", "DialogFragment: onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("MyTAG", "DialogFragment: onCreate");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("MyTAG", "DialogFragment: onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("MyTAG", "DialogFragment: onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e("MyTAG", "DialogFragment: onDetach");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("MyTAG", "DialogFragment: onResume");
    }

    public void onUpdateDialoguesList (DialogueItem dItem) {

        if (dItem == null) {
            Toast.makeText(getContext(), "Fail: try to add null object", Toast.LENGTH_SHORT).show();
            return;
        }

        dialogueAdapter.addItem(dItem);
    }
}