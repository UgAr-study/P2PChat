package com.example.p2pchat;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.p2pchat.ui.dashboard.DashboardFragment;
import com.example.p2pchat.ui.home.HomeFragment;
import com.example.p2pchat.ui.messages.DialogueItem;
import com.example.p2pchat.ui.messages.DialoguesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.net.DatagramSocket;


public class MainActivity extends AppCompatActivity {

    DialoguesFragment dialoguesFragment;
    HomeFragment homeFragment;
    DashboardFragment dashboardFragment;

    public static final String EXTRA_USER_PUBLIC_KEY = "public_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment()).commit();

        setOnClickTestButton();

        if (savedInstanceState != null) {
            //TODO: Restore the fragment's instance
            dialoguesFragment = (DialoguesFragment) getSupportFragmentManager().getFragment(savedInstanceState, "myFragmentName");
            return;
        }

        dialoguesFragment = DialoguesFragment.getDialoguesFragment(this);
        homeFragment = new HomeFragment();
        dashboardFragment = new DashboardFragment();
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

    public void setOnClickTestButton() {
        Button test_btn = findViewById(R.id.test_button);
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialoguesFragment.onUpdateDialoguesList(new DialogueItem("User", "Test Text", "00:00"));
                Toast.makeText(MainActivity.this, "TEST", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //TODO: Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, "myFragmentName", dialoguesFragment);
    }
}