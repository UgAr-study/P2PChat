package com.example.p2pchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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


public class MainActivity extends AppCompatActivity implements DialoguesRecyclerViewAdapter.OnItemClickListener {

    DialoguesFragment dialoguesFragment;
    HomeFragment homeFragment;
    DashboardFragment dashboardFragment;

    public static final String EXTRA_USER_PUBLIC_KEY = "public_key";
    public static final String EXTRA_USER_NAME = "user_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        dialoguesFragment = DialoguesFragment.getDialoguesFragment(this, this);
        homeFragment = new HomeFragment();
        dashboardFragment = new DashboardFragment();

        navView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment).commit();

        setOnClickTestButton();
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



    @Override
    public void onItemClick(DialogueItem item) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(EXTRA_USER_PUBLIC_KEY, item.getUserPublicKey());

        Toast.makeText(MainActivity.this, item.getUserPublicKey(), Toast.LENGTH_SHORT).show();

        startActivity(intent);
    }
}