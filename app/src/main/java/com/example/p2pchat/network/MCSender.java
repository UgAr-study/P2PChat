package com.example.p2pchat.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class MCSender {

    private final int port = 1234;
    private final String host = "229.1.2.3";


    private DatagramSocket socket;
    private InetAddress inetAddress;
    private byte[] helloMessage;

    private Completable observable;

    public MCSender (String toPublicKye, String fromPublicKye, String fromName) {

        helloMessage = (toPublicKye + "\n" + fromName + "\n" + fromPublicKye).getBytes();

        observable = Completable.create(emmit -> {
                try {
                    Connect();
                    SendMCMessage();
                    Close();
                    emmit.onComplete();
                } catch (Exception e) {
                    emmit.onError(e);
                }
        });
    }

    public Completable getObservable() {
        return observable;
    }

    private void Connect() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        inetAddress = InetAddress.getByName(host);
    }

    private void SendMCMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(helloMessage, helloMessage.length, inetAddress, port);
        socket.send(packet);
    }

    private void Close() {
        socket.close();
    }
}









///////////////////////////////////////
/*class MultiCastSender extends Thread{

    private static DatagramSocket socket;
    private static InetAddress inetAddress;
    private static String ToPublicKye, FromName, FromPublicKye;
    private static final int port = 1234;

    public MultiCastSender(String to_pk, String from_name, String from_pk) {
        ToPublicKye = to_pk;
        FromName = from_name;
        FromPublicKye = from_pk;
    }

    @Override
    public void run (){

        try {
            //get connection with this group
            //System.out.println("MC Connecting");
            Connect("229.1.2.3");

            //send hello message to all
            //System.out.println("MC Sending");
            SendMultiCastHello();

            //close socket, cause he is unused
            //System.out.println("MC losing");
            CloseSocket();
        } catch (Exception e) {
            //TODO add handler and send the error message to it
            return;
        }
    }

    private void Connect (String host) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        inetAddress = InetAddress.getByName(host);
    }

    private void SendMultiCastHello () throws IOException {
        String helloMessage = ToPublicKye + "\n" + FromName + "\n" + FromPublicKye;
        byte[] bytes = helloMessage.getBytes();

        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, inetAddress, port);
        socket.send(packet);
    }

    private void CloseSocket () throws Exception {
        socket.close();
    }

}*/

