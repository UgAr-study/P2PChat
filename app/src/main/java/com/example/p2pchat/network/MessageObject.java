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
    private final String secureMac;
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

    public String decrypt(String aesKey) throws NoSuchPaddingException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        if (action == SEND_MESSAGE) {
            String decryptMsg = SymCryptography.decryptMsg(SymCryptography.getSecretKeyByString(aesKey), msg);
            if (!SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(aesKey), decryptMsg).equals(secureMac)) {
                return null;
            } else {
                return decryptMsg;
            }
        } else {
            Log.e("MyTag|MessageObject", "an attempt to decrypt with the wrong key");
            return null;
        }
    }

    public String decryptAesMsg(String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        if (action != SEND_AES_KEY) {
            Log.e("MyTag|MessageObject", "an attempt to decrypt with the wrong key");
            return null;
        }

        PrivateKey privateKeyString = AsymCryptography.getPrivateKeyFromString(privateKey);
        String aesKeyString = AsymCryptography.decryptMsg(msg, privateKeyString);

        if (aesKeyString == null) {
            Log.e("MyTag|Crypto", "Error in decrypt aesKey");
            throw new ExceptionInInitializerError();
        }

        PublicKey pubKeyString = AsymCryptography.getPublicKeyFromPrivateKey(privateKeyString);
        String currentSecureMac = SymCryptography.getMacMsg(pubKeyString, aesKeyString);
        if (!currentSecureMac.equals(secureMac)) {
            return null;
        } else {
            return aesKeyString;
        }
    }
}
