package com.example.p2pchat.network;

import android.util.Log;

import com.example.p2pchat.security.AsymCryptography;
import com.example.p2pchat.security.SymCryptography;

import java.io.IOException;
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
import javax.crypto.SecretKey;

public class MessageObject {
    private final String from;
    private final SealedObject msg;
    private final byte[] secureMac;
    private boolean keyFlag = false;

    static final public boolean SEND_AES_KEY = true;

    public MessageObject(String from, String msg, String aesKey) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        this.from = from;
        this.msg = SymCryptography.encryptMsg(SymCryptography.getSecretKeyByString(aesKey), msg);
        this.secureMac = SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(aesKey), msg);
    }

    public MessageObject(String from, String toPubKey, String aesKey, boolean keyOrNot) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        if (!keyOrNot) {
            Log.e("MyTeg|MessageObject", "Construct aes key msg, with flase flag");
            throw new ExceptionInInitializerError();
        }
        msg = AsymCryptography.encryptMsg(aesKey, AsymCryptography.getPublicKeyFromString(toPubKey));
        secureMac = SymCryptography.getMacMsg(AsymCryptography.getPublicKeyFromString(toPubKey), aesKey);

        this.from = from;
        keyFlag = SEND_AES_KEY;
        keyFlag = true;
    }

    public boolean isKeyMsg() {
        return keyFlag;
    }

    public String from() {
        return from;
    }

    public String decrypt(String key) throws NoSuchPaddingException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        if (!keyFlag) {
            String decryptMsg = SymCryptography.decryptMsg(SymCryptography.getSecretKeyByString(key), msg);
            if (!Arrays.equals(SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(key), decryptMsg), secureMac)) {
                return null;
            } else {
                return decryptMsg;
            }
        }  else {
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
