package com.example.p2pchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.p2pchat.security.AsymCryptography;
import com.example.p2pchat.security.SymCryptography;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.PublicKey;
import java.util.Enumeration;

import javax.crypto.SealedObject;

public class LoginActivity extends AppCompatActivity {

    private EditText loginField;
    private EditText passwordField;
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_LOGIN = "login";
    public static final String EXTRA_PABLIC_KEY = "public_key";
    SharedPreferences userInfo;
    SharedPreferences kesStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginField    = findViewById(R.id.login);
        passwordField = findViewById(R.id.password);

        //TextView locIp = findViewById(R.id.localAddress);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        userInfo = getSharedPreferences("UsersRegistrationData", MODE_PRIVATE);

        //locIp.setText(getLocalIp());
    }

    public void onClickSignUpButton (View v) {
        if (loginField.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Error: login is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordField.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Error: password is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = loginField.getText().toString();
        String password = passwordField.getText().toString();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_PASSWORD, password);

        if (userInfo.getAll().isEmpty()) {
            SharedPreferences.Editor userInfoEditor = userInfo.edit();
            try {
                String encryptPwd = SymCryptography.getStringHash(password);

                userInfoEditor.putString(name, encryptPwd);
                userInfoEditor.apply();
            } catch (Exception e) {
                Toast.makeText(this, "Sym Crypto ERROR\n", Toast.LENGTH_LONG).show();
                return;
            }

            String userPublicKye = AsymCryptography.getStringAsymKey(generateNewPairAsymKey(password));

            intent.putExtra(EXTRA_LOGIN, name);
            intent.putExtra(EXTRA_PABLIC_KEY, userPublicKye);

            startActivity(intent);

        } else {
            if (!userInfo.contains(name)) {
                Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show();
                return;
            } else {
                try {
                    String encryptPwd = userInfo.getString(name, null);
                    if (SymCryptography.getStringHash(password).equals(encryptPwd)) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Invalid password", Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Invalid password", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    private String getLocalIp () {

        String res = "";
        try {
            Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();  // gets All networkInterfaces of your device
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface inet = (NetworkInterface) networkInterfaces.nextElement();
                Enumeration address = inet.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) address.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        res =  res.concat(inetAddress.getHostAddress() + "\n");
                    }
                }
            }
        } catch (Exception e) {
            res = e.getMessage();
        }

        return res;
    }

    private PublicKey generateNewPairAsymKey(String pwd) {
        kesStore = getSharedPreferences(AsymCryptography.KEY_STORE_NAME, MODE_PRIVATE);
        return AsymCryptography.generateAndSaveNewPair(pwd, kesStore);
    }

}