package com.example.p2pchat.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.crypto.SealedObject;

import io.reactivex.rxjava3.core.Completable;

public class TCPSender {

    private final int serverPort = 4000;
    private final int TIME_OUT = 1000;

    private Socket socket;
    private String ipAddress;
    private MessageObject message;

    private ObjectOutputStream out;
    private Completable observable;

    public TCPSender(MessageObject recipientMessage, String recipientIpAddress) {

        message = recipientMessage;
        ipAddress = recipientIpAddress;

        observable = Completable.create(emmit -> {
            try {
                Connect();
                SendMessage();
                Close();
                emmit.onComplete();
            } catch (Exception e) {
                Close();
                emmit.onError(e);
            }
        });
    }

    public Completable getObservable() {
        return observable;
    }

    private void Connect() throws IOException {

        InetAddress ipAddr = InetAddress.getByName(ipAddress);
        socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddr, serverPort), TIME_OUT);
        //TODO: if socket didn't connect, what is next?
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    private void SendMessage() throws IOException {
        out.writeObject(message);
        out.flush();
    }

    private void Close() throws IOException {
        out.close();
        socket.close();
    }
}







//////////////////////////////////////////

/*class Messenger {

    private final int serverPort = 4000;
    private Socket socket;
    private ObjectOutputStream out;

    public boolean SendMessageToIp (String message, String ipAddress) {

        try {
            InetAddress ipAddr = InetAddress.getByName(ipAddress);

            socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddr, serverPort), 1000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(message);
            out.flush();
            out.close();

            return true;

        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean SendEncryptMessageToIp (SealedObject encryptMsg, String ipAddress) {
        try {
            InetAddress ipAddr = InetAddress.getByName(ipAddress);
            socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddr, serverPort), 1000);
            out = new ObjectOutputStream(socket.getOutputStream());

            out.writeObject(encryptMsg);
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void Close() {
        try {
            out.close();
            socket.close();
        } catch (IOException e) {
            //do nothing
        }
    }
}*/