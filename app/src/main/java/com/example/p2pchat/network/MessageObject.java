package com.example.p2pchat.network;

import android.util.Log;

import com.example.p2pchat.security.AsymCryptography;
import com.example.p2pchat.security.SymCryptography;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

public class MessageObject implements Serializable {
    private final String fromPublicKey;
    private final SealedObject msg;
    private final byte[] secureMac;
    private boolean action;

    public static final  boolean SEND_AES_KEY = true;
    public static final  boolean SEND_MESSAGE = false;

    public MessageObject(String fromPublicKey, String messageOrToPubKey, String aesKey, boolean action) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

        if (action == SEND_AES_KEY) {
            this.msg = AsymCryptography.encryptMsg(aesKey, AsymCryptography.getPublicKeyFromString(messageOrToPubKey));
            this.secureMac = SymCryptography.getMacMsg(AsymCryptography.getPublicKeyFromString(messageOrToPubKey), aesKey);

            this.fromPublicKey = fromPublicKey;
        } else {
            this.fromPublicKey = fromPublicKey;
            this.msg = SymCryptography.encryptMsg(SymCryptography.getSecretKeyByString(aesKey), messageOrToPubKey);
            this.secureMac = SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(aesKey), messageOrToPubKey);
        }

        this.action = action;
    }

    public boolean isKeyMsg() {
        return (action == SEND_AES_KEY);
    }

    public String getSenderPublicKey() {
        return fromPublicKey;
    }

    public String decrypt(String aesOrPrivateKey) throws NoSuchPaddingException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        if (action == SEND_MESSAGE) {
            String decryptMsg = SymCryptography.decryptMsg(SymCryptography.getSecretKeyByString(aesOrPrivateKey), msg);
            if (!Arrays.equals(SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(aesOrPrivateKey), decryptMsg), secureMac)) {
                return null;
            } else {
                return decryptMsg;
            }
        } else {
            PrivateKey myPrivateKey = AsymCryptography.getPrivateKeyFromString(aesOrPrivateKey);
            String aesKey = AsymCryptography.decryptMsg(msg, myPrivateKey);

            if (aesKey == null) {
                Log.e("MyTag|Crypto", "Error in decrypt aesKey");
                throw new ExceptionInInitializerError();
            }

            PublicKey myPubKey = AsymCryptography.getPublicKeyFromPrivateKey(myPrivateKey);
            if (!Arrays.equals(SymCryptography.getMacMsg(myPubKey, aesKey), secureMac)) {
                return null;
            } else {
                return aesKey;
            }
        }
    }
}
