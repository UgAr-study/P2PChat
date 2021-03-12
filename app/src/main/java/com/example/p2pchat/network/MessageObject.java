package com.example.p2pchat.network;

import com.example.p2pchat.security.AsymCryptography;
import com.example.p2pchat.security.SymCryptography;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    public MessageObject(String from, String to, String msg, String aesKey) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        this.from = from;
        this.msg = SymCryptography.encryptMsg(SymCryptography.getSecretKeyByString(aesKey), msg);
        this.secureMac = SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(aesKey), msg);
    }

    public MessageObject(String from, String to, String msg, String aesKey, boolean keyOrNot) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        this(from, to, msg, aesKey);
        keyFlag = true;
    }

    public boolean isKeyMsg() {
        return keyFlag;
    }

    public String from() {
        return from;
    }

    public String decrypt(String aesKey) throws NoSuchPaddingException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        String decryptMsg = SymCryptography.decryptMsg(SymCryptography.getSecretKeyByString(aesKey), msg);
        if (!Arrays.equals(SymCryptography.getMacMsg(SymCryptography.getSecretKeyByString(aesKey), decryptMsg), secureMac)) {
            return null;
        } else {
            return decryptMsg;
        }
    }
}
