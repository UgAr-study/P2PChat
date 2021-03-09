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
import com.example.p2pchat.network.MCSender;
import com.example.p2pchat.network.TCPReceiver;
import com.example.p2pchat.network.TCPSender;
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

import java.net.DatagramSocket;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements DialoguesRecyclerViewAdapter.OnItemClickListener {

    public static final String EXTRA_USER_PUBLIC_KEY = "public_key";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_PASSWORD = "user_password";

    private final String USER_INFO_TABLE_NAME = "UserInfoTable";
    private final String USER_DATA_TABLE_NAME = "UserDataTable";

    private String userPublicKey;
    private String userName;
    private String userPassword;

    private SQLUserInfo UserInfoTable;
    private SQLUserData UserDataTable;
    private SharedPreferences keyStore;

    private DialoguesFragment dialoguesFragment;
    private HomeFragment homeFragment;
    private DashboardFragment dashboardFragment;

    private boolean isMCSenderRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        userPublicKey = intent.getStringExtra(EXTRA_USER_PUBLIC_KEY);
        userName      = intent.getStringExtra(EXTRA_USER_NAME);
        userPassword  = intent.getStringExtra(EXTRA_USER_PASSWORD);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        dialoguesFragment = DialoguesFragment.getDialoguesFragment(this, this);
        homeFragment      = new HomeFragment();
        dashboardFragment = new DashboardFragment();

        navView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment).commit();

        UserInfoTable = new SQLUserInfo(this, USER_INFO_TABLE_NAME);
        UserDataTable = new SQLUserData(this, USER_DATA_TABLE_NAME); //TODO: wait for wrapper

        keyStore = getSharedPreferences(AsymCryptography.KEY_STORE_NAME, MODE_PRIVATE);
        setOnClickTestButton();
        startNetwork();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
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

    private static int test_count = 0;
    public void setOnClickTestButton() {
        Button test_btn = findViewById(R.id.test_button);
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialoguesFragment.onUpdateDialoguesList(new DialogueItem("User", "Test Text", "00:00", String.valueOf(test_count)));
                test_count++;
                Toast.makeText(MainActivity.this, "TEST", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //TODO: Save the fragment's instance
        //getSupportFragmentManager().putFragment(outState, "myFragmentName", dialoguesFragment);
    }


    private void startNetwork() {
        startMCReceiver();

    }

    private void startMCReceiver() {
        MCReceiver mcReceiver = new MCReceiver(UserInfoTable);
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
                new TCPReceiver(this, UserInfoTable, userPassword, keyStore);

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
                    String message = item.getIpAddress();
                    String testTime = "00:00";
                    dialoguesFragment.onUpdateDialoguesList(new DialogueItem(item.getName(), message, testTime,item.getPublicKey()));
                } else {
                    //TODO: implement onUpgradeState method
                    //dialoguesFragment.onUpdateState(item.getPublicKey());
                }

                if (!isMCSenderRun) {
                    isMCSenderRun = true;
                    startInterrogator();
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

    private Observer<MessageItem> getTCPReceiverObserver() {
        return new Observer<MessageItem>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                // do nothing yet
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull MessageItem messageItem) {

            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }


    @Override
    public void onItemClick(DialogueItem item) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(EXTRA_USER_PUBLIC_KEY, item.getUserPublicKey());
        intent.putExtra(EXTRA_USER_NAME, item.getName());

        Toast.makeText(MainActivity.this, item.getUserPublicKey(), Toast.LENGTH_SHORT).show();

        startActivity(intent);
    }
}