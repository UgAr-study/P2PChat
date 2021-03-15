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
    private final String TYPE_KEY = "key";
    private final String TYPE_MESSAGE = "message";

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

    //TODO: what to do if this is new user, which we don't have in our table, but he does?
    private String[] ReceiveMessage() throws IOException, ClassNotFoundException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        encryptedMessage = (MessageObject) inputStream.readObject();

        String fromPublicKey = encryptedMessage.getSenderPublicKey();
        String aesKey, type, message;

        if (encryptedMessage.isKeyMsg()) {

            String privateKey = getOwnerPrivateKey();
            aesKey = encryptedMessage.decrypt(privateKey);
            message = aesKey;
            type = TYPE_KEY;

        } else {

            aesKey = UserInfoTable.getAESKeyByPublicKey(fromPublicKey).get(0);
            message = encryptedMessage.decrypt(aesKey);
            type = TYPE_MESSAGE;
        }

        if (fromPublicKey == null || aesKey == null || message == null)
            return null;

        return new String[] {message, fromPublicKey, type};
    }

    private MessageInfo ParseMessage(String[] rcvMessage) {

        if (rcvMessage == null)
            return null;

        String message       = rcvMessage[0];
        String fromPublicKey = rcvMessage[1];
        String type          = rcvMessage[2];

        if (type.equals(TYPE_MESSAGE)) {

            String name   = UserInfoTable.getNameByPublicKey(fromPublicKey).get(0);
            String id     = UserInfoTable.getIdByPublicKey(fromPublicKey).get(0);
            Calendar time = getCurrentTime();

            MessageItem msgItem = new MessageItem(name, message, time);
            MessageInfo messageInfo = new MessageInfo(msgItem, id, fromPublicKey);
            addToDialogueTable(msgItem, id);
            return messageInfo;

        } else {
            UserInfoTable.updateAESKeyByPublicKey(message, fromPublicKey);
            return null;
        }
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

    private String getOwnerPrivateKey() {
        //TODO: get real private key

        String privateKey = AsymCryptography.loadPrivateKeyFromKeyStore(userPassword, AsymCryptography.getKeyStore());

        if (privateKey == null) {
            Log.e ("TCPReceiver", "getOwnerPrivateKey: private key is null");
        }

        return privateKey;
    }
}