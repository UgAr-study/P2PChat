package com.example.p2pchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.p2pchat.dataTools.SQLUserInfo;
import com.example.p2pchat.security.AsymCryptography;
import com.example.p2pchat.security.SymCryptography;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.PublicKey;
import java.util.Enumeration;

public class LoginActivity extends AppCompatActivity {

    private EditText loginField;
    private EditText passwordField;
    public static final String USER_INFO_TABLE_NAME = "UserInfoTable";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_LOGIN = "login";
    public static final String EXTRA_PUBLIC_KEY = "public_key";
    SharedPreferences loginData;
    SharedPreferences kesStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginField    = findViewById(R.id.login);
        passwordField = findViewById(R.id.password);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        loginData = getSharedPreferences("UsersRegistrationData", MODE_PRIVATE);
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

        if (loginData.getAll().isEmpty()) {
            SharedPreferences.Editor userInfoEditor = loginData.edit();
            try {
                String encryptPwd = SymCryptography.getStringHash(password);

                userInfoEditor.putString(name, encryptPwd);
                userInfoEditor.apply();
            } catch (Exception e) {
                Toast.makeText(this, "Sym Crypto ERROR\n", Toast.LENGTH_LONG).show();
                return;
            }

            String userPublicKey = AsymCryptography.getStringAsymKey(generateNewPairAsymKey(password));
            SQLUserInfo sqlUserInfo = new SQLUserInfo(this, USER_INFO_TABLE_NAME);
            String localIP = getLocalIp();
            sqlUserInfo.WriteDB(name,  localIP, userPublicKey);

            intent.putExtra(EXTRA_LOGIN, name);
            intent.putExtra(EXTRA_PUBLIC_KEY, userPublicKey);

            startActivity(intent);

        } else {
            if (!loginData.contains(name)) {
                Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show();
                return;
            } else {
                try {
                    String encryptPwd = loginData.getString(name, null);
                    if (SymCryptography.getStringHash(password).equals(encryptPwd)) {
                        intent.putExtra(EXTRA_LOGIN, name);
                        SQLUserInfo sqlUserInfo = new SQLUserInfo(this, USER_INFO_TABLE_NAME);
                        String adminId = "1";
                        String adminPublicKey = sqlUserInfo.getPublicKeyById(adminId);
                        intent.putExtra(EXTRA_PUBLIC_KEY, adminPublicKey);
                        sqlUserInfo.updateIpByPublicKey(getLocalIp(), adminPublicKey);

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