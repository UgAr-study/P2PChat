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
import java.security.spec.InvalidKeySpecException;
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

    public MessageObject(String fromPublicKey, String message, String key, boolean action) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, InvalidKeySpecException {
        this.fromPublicKey = fromPublicKey;

        if (action == SEND_AES_KEY) {
            this.msg = AsymCryptography.encryptMsg(message, AsymCryptography.getPublicKeyFromString(key));
            this.secureMac = SymCryptography.getMacMsg(AsymCryptography.getPublicKeyFromString(key), message);
        } else {
            this.msg = SymCryptography.encryptMsg(SymCryptography.getSecretKeyByString(key), message);
            this.secureMac = SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(key), message);
        }

        this.action = action;
    }

    public boolean isKeyMsg() {
        return (action == SEND_AES_KEY);
    }

    public String getSenderPublicKey() {
        return fromPublicKey;
    }

    public String decrypt(String key) throws NoSuchPaddingException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        if (action == SEND_MESSAGE) {
            String decryptMsg = SymCryptography.decryptMsg(SymCryptography.getSecretKeyByString(key), msg);
            if (!Arrays.equals(SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(key), decryptMsg), secureMac)) {
                return null;
            } else {
                return decryptMsg;
            }
        } else {
            PrivateKey myPrivateKey = AsymCryptography.getPrivateKeyFromString(key);
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
