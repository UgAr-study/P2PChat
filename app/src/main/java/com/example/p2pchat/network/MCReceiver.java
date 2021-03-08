package com.example.p2pchat.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.p2pchat.dataTools.SQLUserInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.core.Observable;

public class MCReceiver {

    private MulticastSocket socket;
    private final int port = 1234;
    private String ip = "229.1.2.3";
    private SQLUserInfo UserInfoTable;
    private String myName;
    private String myPublicKey;
    private InetAddress group;
    private byte[] buffer;
    private ArrayList<String> requestMsg;

    private Observable<UserInfo> observable;

    public MCReceiver (SQLUserInfo sqlUserInfo) {

        UserInfoTable = sqlUserInfo;
        myName = UserInfoTable.getNameById(String.valueOf(1)).get(0);
        myPublicKey = UserInfoTable.getPublicKeyById(String.valueOf(1)).get(0);

        buffer = new byte[8192];

        observable = Observable.create(emmit -> {

            try {
                Connect();

                while (true) {
                    requestMsg = ReceiveMessage();
                    ParseMessage(requestMsg);
                    emmit.onNext(item);
                }

            } catch (Exception e) {
                Close();
                emmit.onError(e);
            }
        });
    }

    public Observable<UserInfo> getObservable() {
        return observable;
    }

    private void Connect() throws IOException {
        socket = new MulticastSocket(port);
        group = InetAddress.getByName(ip);
        socket.joinGroup(group);
    }

    private ArrayList<String> ReceiveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        String receivedMsg = new String(packet.getData(), 0, packet.getLength());
        String senderIpAddress = packet.getAddress().toString().substring(1);

        ArrayList<String> res = new ArrayList<>(Arrays.asList(receivedMsg.split("\n")));
        res.add(senderIpAddress);
        return res;
    }

    private void ParseMessage(String[] requestMsg) {

        if (requestMsg.length == 4) {

            String toPublicKey = requestMsg[0];
            String fromName = requestMsg[1];
            String fromPublicKey = requestMsg[2];
            String senderIpAddress = requestMsg[3];
        }
    }

    private void Close() {

    }
}

class UserInfo {

    private String name;
    private String publicKey;
    private String ipAddress;

    public UserInfo (String name, String publicKey, String ipAddress) {
        this.name = name;
        this.publicKey = publicKey;
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}





















////////////////////////////////////////////

class MultiCastReceiver extends Thread {


    private final int port = 1234;
    private final int ERROR = 1;
    private final int SUCCESS = 0;
    private final String ip = "229.1.2.3";
    private final String KEY_DATA = "Data";
    private final String KEY_ERROR = "ErrorMsg";

    private MulticastSocket socket;
    private SQLUserInfo sqlTable;
    private String myName;
    private String myPublicKey;
    private InetAddress group;
    private Handler mHandler;

    public MultiCastReceiver(SQLUserInfo tableInput, Handler handler) {
        sqlTable    = tableInput;
        myName      = sqlTable.getNameById(String.valueOf(1)).get(0);
        myPublicKey = sqlTable.getPublicKeyById(String.valueOf(1)).get(0);
        mHandler    = handler;
    }

    public void run() {

        try {
            socket = new MulticastSocket(port);
            group = InetAddress.getByName(ip);
            socket.joinGroup(group);
        } catch (IOException e){
            Message msg = mHandler.obtainMessage();

            Bundle bundle = new Bundle();
            bundle.putString(KEY_ERROR, e.getMessage());

            msg.setData(bundle);
            msg.what = ERROR;
            mHandler.sendMessage(msg);
            return;
        }

        byte[] buf = new byte[8192];

        while(true) {

            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                Message msg = mHandler.obtainMessage();

                Bundle bundle = new Bundle();
                bundle.putString(KEY_ERROR, e.getMessage());

                msg.setData(bundle);
                msg.what = ERROR;
                mHandler.sendMessage(msg);
                continue;
            }

            String receivedMsg = new String(packet.getData(), 0, packet.getLength());

            String[] subString = receivedMsg.split("\n");

            if (subString.length < 3) {
                Message msg = mHandler.obtainMessage();

                Bundle bundle = new Bundle();
                bundle.putString(KEY_ERROR, "Wrong message structure");

                msg.setData(bundle);
                msg.what = ERROR;
                mHandler.sendMessage(msg);
                continue;
            } else {
                Message msg = mHandler.obtainMessage();

                Bundle bundle = new Bundle();
                bundle.putString(KEY_DATA, receivedMsg);

                msg.setData(bundle);
                msg.what = SUCCESS;
                mHandler.sendMessage(msg);
            }

            String toPublicKey = subString[0];
            String fromName = subString[1];
            String fromPublicKey = subString[2];

            String senderIpAddress = packet.getAddress().toString().substring(1);

            // Add authorized user
            if (toPublicKey.equals("all")) {

                if (fromPublicKey.equals(myPublicKey)) {

                    int nrows = sqlTable.updateIpByPublicKey(senderIpAddress, myPublicKey);

                    Message msg = mHandler.obtainMessage();

                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_DATA, String.valueOf(nrows) + " are updated!");

                    msg.setData(bundle);
                    msg.what = SUCCESS;

                    mHandler.sendMessage(msg);

                    continue;
                }

                if (sqlTable.getNameByPublicKey(fromPublicKey).isEmpty()) {
                    //String ip = packet.getAddress().toString().substring(1);
                    sqlTable.WriteDB(fromName, senderIpAddress, fromPublicKey);
                    mHandler.sendEmptyMessage(SUCCESS);
                }

                new MultiCastSender(fromPublicKey, myName, myPublicKey).start();

            } else if (toPublicKey.equals(myPublicKey) && sqlTable.getNameByPublicKey(fromPublicKey).isEmpty()) {
                sqlTable.WriteDB(fromName, senderIpAddress, fromPublicKey);
                mHandler.sendEmptyMessage(SUCCESS);
            }
        }
    }

    public void close() {
        socket.close();
    }
}





