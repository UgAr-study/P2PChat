package com.example.p2pchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.p2pchat.dataTools.SQLUserData;
import com.example.p2pchat.dataTools.SQLUserInfo;
import com.example.p2pchat.network.Interrogator;
import com.example.p2pchat.network.MCReceiver;
import com.example.p2pchat.network.MessageInfo;
import com.example.p2pchat.network.TCPReceiver;
import com.example.p2pchat.network.UserInfo;
import com.example.p2pchat.security.AsymCryptography;
import com.example.p2pchat.ui.chat.MessageItem;
import com.example.p2pchat.ui.dashboard.DashboardFragment;
import com.example.p2pchat.ui.home.HomeFragment;
import com.example.p2pchat.ui.messages.DialogueItem;
import com.example.p2pchat.ui.messages.DialoguesFragment;
import com.example.p2pchat.ui.messages.DialoguesRecyclerViewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements DialoguesRecyclerViewAdapter.OnItemClickListener {

    public static final String EXTRA_USER_PUBLIC_KEY = "public_key";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_PASSWORD = "user_password";
    public static final String EXTRA_USER_INFO_TABLE = "user_info_table";
    public static final String USER_INFO_TABLE_NAME = "UserInfoTable";
    public static final String EXTRA_USER_ID = "user_id";

    private String userPublicKey;
    private String userName;
    private String userPassword;

    private SQLUserInfo UserInfoTable;
    private SharedPreferences keyStore;

    private DialoguesFragment dialoguesFragment;
    private HomeFragment homeFragment;
    private DashboardFragment dashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIntents();
        defineFragments();
        accessStorages();
        setUsersInDialogueFragment();
        startNetwork();
        setOnClickTestButton();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            selectedFragment = homeFragment;
                            break;

                        case R.id.navigation_dashboard:
                            selectedFragment = dashboardFragment;
                            break;

                        case R.id.navigation_dialogues:
                            selectedFragment = dialoguesFragment;
                            break;
                    }

                    if (selectedFragment != null)
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment).addToBackStack(null).commit();

                    return true;
                }
            };

    private void getIntents() {
        Intent intent = getIntent();
        userPublicKey = intent.getStringExtra(EXTRA_USER_PUBLIC_KEY);
        userName      = intent.getStringExtra(EXTRA_USER_NAME);
        userPassword  = intent.getStringExtra(EXTRA_USER_PASSWORD);

/*
        //TODO: change to above
        userPublicKey = "publicKey";
        userName      = "userName";
        userPassword  = "password";
*/
    }

    private void defineFragments() {
        BottomNavigationView navView = findViewById(R.id.nav_view);

        dialoguesFragment = DialoguesFragment.getDialoguesFragment(this, this);
        homeFragment      = new HomeFragment();
        dashboardFragment = new DashboardFragment();

        navView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment).commit();
    }

    private void accessStorages() {
        UserInfoTable = new SQLUserInfo(this, USER_INFO_TABLE_NAME);
        keyStore = getSharedPreferences(AsymCryptography.KEY_STORE_NAME, MODE_PRIVATE);
    }

    private void setUsersInDialogueFragment () {
        Single<ArrayList<DialogueItem>> singleObservable = Single.create(emmit -> {
            try {
                ArrayList<String> names = UserInfoTable.getAllNames();
                ArrayList<String> publicKeys = UserInfoTable.getAllPublicKeys();

                ArrayList<DialogueItem> dialogueItems = new ArrayList<>();

                for (int i = 0, end = names.size(); i != end; ++i)
                    dialogueItems.add(new DialogueItem(names.get(i), "", "00:00", publicKeys.get(i)));

                emmit.onSuccess(dialogueItems);
            } catch (Exception e) {
                emmit.onError(e);
            }
        });

        singleObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getFillDialoguesObserver());
    }

    private static int test_count = 0;
    public void setOnClickTestButton() {
        Button test_btn = findViewById(R.id.test_button);
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialoguesFragment.onUpdateDialoguesList(new DialogueItem(userName, "Test Text", "00:00", userPublicKey));
                test_count++;
                Toast.makeText(MainActivity.this, "TEST", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startNetwork() {
        startMCReceiver();
        startTCPReceiver();
        startInterrogator();
    }

    private void startMCReceiver() {
        MCReceiver mcReceiver = new MCReceiver(UserInfoTable, userName, userPublicKey);
        mcReceiver.getObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getMCReceiverObserver());
    }

    private void startInterrogator() {
        Interrogator interrogator = new Interrogator(userPublicKey, userName);
        interrogator.getObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getInterrogatorObserver());
    }

    private void startTCPReceiver() {
        TCPReceiver tcpReceiver =
                new TCPReceiver(this, UserInfoTable, userPassword, keyStore); //TODO: should i change context to ChatActivity context?

        tcpReceiver.getObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getTCPReceiverObserver());
    }

    private Observer<UserInfo> getMCReceiverObserver() {
        return new Observer<UserInfo>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                //do nothing yet
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull UserInfo item) {

                if (item.isNewUser()) {
                    //TODO delete test info: message & testTime
                    String testMessage = item.getIpAddress();
                    String testTime = "00:00";
                    dialoguesFragment.onUpdateDialoguesList(new DialogueItem(item.getName(), testMessage, testTime, item.getPublicKey()));
                } else {
                    //TODO: implement onUpgradeState method
                    //dialoguesFragment.onUpdateState(item.getPublicKey());
                }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MyTag", e.getMessage()); //TODO delete it
            }

            @Override
            public void onComplete() {
                String text = "MCReceiver has completed";
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                Log.e("MyTag", text); //TODO delete it
            }
        };
    }

    private Observer<Boolean> getInterrogatorObserver() {
        return new Observer<Boolean>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                //do nothing yet
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                //do nothing
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MyTag", e.getMessage()); //TODO delete it
            }

            @Override
            public void onComplete() {
                String text = "Interrogator has completed";
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                Log.e("MyTag", text); //TODO delete it
            }
        };
    }

    private Observer<MessageInfo> getTCPReceiverObserver() {
        return new Observer<MessageInfo>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                // do nothing yet
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull MessageInfo messageInfo) {
                // show new message to user
                dialoguesFragment.onUpdateLastMessage(messageInfo);
                ChatActivity.loadNewMsg(messageInfo);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MyTag", e.getMessage()); //TODO delete it
            }

            @Override
            public void onComplete() {
                String text = "TCPReceiver has completed";
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                Log.e("MyTag", text); //TODO delete it
            }
        };
    }

    private SingleObserver<ArrayList<DialogueItem>> getFillDialoguesObserver() {
        return new SingleObserver<ArrayList<DialogueItem>>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                // do nothing
            }

            @Override
            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull ArrayList<DialogueItem> dialogueItems) {
                for (DialogueItem dItem: dialogueItems)
                    dialoguesFragment.onUpdateDialoguesList(dItem);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MyTag", e.getMessage()); //TODO delete it
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //TODO: Save the fragment's instance
    }

    @Override
    public void onItemClick(DialogueItem item) {
        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra(ChatActivity.EXTRA_RECIPIENT_PUBKEY, item.getUserPublicKey());
        intent.putExtra(ChatActivity.EXTRA_SENDER_PUBKEY, userPublicKey);
        intent.putExtra(ChatActivity.EXTRA_RECIPIENT_NAME, item.getName());
        intent.putExtra(ChatActivity.EXTRA_RECIPIENT_ID, UserInfoTable.getIdByPublicKey(item.getUserPublicKey()).get(0));
        intent.putExtra(ChatActivity.EXTRA_AES_KEY, UserInfoTable.getAESKeyByPublicKey(item.getUserPublicKey()).get(0));
        intent.putExtra(EXTRA_USER_INFO_TABLE, USER_INFO_TABLE_NAME);

        Toast.makeText(MainActivity.this, item.getUserPublicKey(), Toast.LENGTH_SHORT).show();

        startActivity(intent);
    }
}