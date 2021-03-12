package com.example.p2pchat.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import androidx.cardview.widget.CardView;

import com.example.p2pchat.dataTools.SQLUserData;
import com.example.p2pchat.dataTools.SQLUserInfo;
import com.example.p2pchat.security.AsymCryptography;
import com.example.p2pchat.security.SymCryptography;
import com.example.p2pchat.ui.chat.MessageItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.internal.operators.observable.ObservableReduceMaybe;

public class TCPReceiver {

    private final int port = 4000;

    private String userPassword;
    private MessageObject encryptedMessage; //TODO: change to Pair <SealObject, PublicKey>

    private Socket socket;
    private ServerSocket serverSocket;
    private ObjectInputStream inputStream;

    private Context context;

    private SQLUserInfo UserInfoTable;
    private SharedPreferences userKeyStore;

    private Observable<MessageInfo> observable;

    public TCPReceiver(Context mainContext,
                       SQLUserInfo userInfoTable,
                       String password,
                       SharedPreferences keyStore) {

        context       = mainContext;
        UserInfoTable = userInfoTable;
        userPassword  = password;
        userKeyStore  = keyStore;

        observable = Observable.create(emmit -> {

            try {
                CreateSocket();

                while (true) {
                    Connect();
                    String[] rcvMessage = ReceiveMessage();
                    MessageInfo item = ParseMessage(rcvMessage);
                    CloseSocket();

                    if (item != null)
                        emmit.onNext(item);
                }

            } catch (Exception e) {
                CloseAll();
                emmit.onError(e);
            }
        });
    }

    public Observable<MessageInfo> getObservable() {
        return observable;
    }

    private void CreateSocket() throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private void Connect() throws IOException {
        socket = serverSocket.accept();
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    private String[] ReceiveMessage() throws IOException, ClassNotFoundException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        encryptedMessage = (MessageObject) inputStream.readObject();

        String fromPublicKey = encryptedMessage.from();
        String aesKey        = UserInfoTable.getAESKeyByPublicKey(fromPublicKey).get(0);
        String message       = encryptedMessage.decrypt(aesKey);

        if (fromPublicKey == null || aesKey == null || message == null)
            return null;

        return new String[] {message, fromPublicKey};
    }

    private MessageInfo ParseMessage(String[] rcvMessage) {

        if (rcvMessage == null)
            return null;

        String message       = rcvMessage[0];
        String fromPublicKey = rcvMessage[1];
        String name          = UserInfoTable.getNameByPublicKey(fromPublicKey).get(0);
        String id            = UserInfoTable.getIdByPublicKey(fromPublicKey).get(0);

        Calendar time        = getCurrentTime();

        MessageItem msgItem = new MessageItem(name, message, time);
        MessageInfo messageInfo = new MessageInfo(msgItem, id, fromPublicKey);
        addToDialogueTable(msgItem, id);
        return messageInfo;
    }

    private void CloseSocket() throws IOException {
        socket.close();
    }

    private void CloseAll () throws IOException {
        socket.close();
        serverSocket.close();
    }

    private Calendar getCurrentTime() {
        return new GregorianCalendar();
    }

    private void addToDialogueTable(MessageItem item, String id) {
        SQLUserData.insertByIdentifier(id, item, context);
    }
}








////////////////////////////////////////////////////
/*class TCPReceiverE extends Thread {

    private Socket socket;
    private ServerSocket serverSocket;
    private final int port = 4000;
    private Handler mHandler;
    private SQLUserInfo UserTable;
    private String userPassword;
    private SharedPreferences keyStore;

    private final int ERROR = 1;
    private final int SUCCESS = 0;
    private final String KEY_DATA = "Data";
    private final String KEY_NAME = "Name";
    private final String KEY_ERROR = "ErrorMsg";

    public TCPReceiverE(Handler handler, SQLUserInfo db, String pwd, SharedPreferences kStore) {
        mHandler = handler;
        UserTable = db;
        userPassword = pwd;
        keyStore = kStore;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {

                socket = serverSocket.accept();

                String text;

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                SealedObject sobj = (SealedObject) in.readObject();
                String ipAddress = socket.getInetAddress().getHostAddress();

                ArrayList<String> aesKeys = UserTable.getAESKeyByIpAddress(ipAddress);

                boolean isAESKeyInTable = false;
                for (int i = 0; i < aesKeys.size(); ++i)
                    if (aesKeys.get(i) != null)
                        isAESKeyInTable = true;

                if (!isAESKeyInTable) {
                    AsymCryptography S = loadPrivateKey(userPassword);

                    if (S == null) {
                        text = "Error: AsymCrypt failed\n";
                    } else {
                        String symKey = S.decryptMsg(sobj);

                        UserTable.updateAESKeyByIpAddress(symKey, ipAddress);

                        sobj = (SealedObject) in.readObject();
                        text = SymCryptography.decryptMsg(sobj, SymCryptography.getSecretKeyByString(symKey));
                    }

                } else {

                    String symKey = aesKeys.get(0);
                    text = SymCryptography.decryptMsg(sobj, SymCryptography.getSecretKeyByString(symKey));
                }



                Message msg = mHandler.obtainMessage();

                Bundle bundle = new Bundle();
                bundle.putString(KEY_DATA, text);
                bundle.putString(KEY_NAME, socket.getInetAddress().getHostAddress());

                msg.setData(bundle);
                msg.what = SUCCESS;
                mHandler.sendMessage(msg);

                socket.close();
            }
        }catch (Exception e) {
            Message msg = mHandler.obtainMessage();

            Bundle bundle = new Bundle();
            bundle.putString(KEY_ERROR, e.getMessage());

            msg.setData(bundle);
            msg.what = ERROR;
            mHandler.sendMessage(msg);

            return;
        } finally {
            try {
                serverSocket.close();
            } catch (Exception e) {
                Log.d("myLog", Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    private AsymCryptography loadPrivateKey(String pwd) {
        AsymCryptography as = new AsymCryptography();
        try {
            as.loadPrivateKey(pwd, keyStore);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | IOException | ClassNotFoundException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException e){
            return null;
        }
        return as;
    }
}*/
