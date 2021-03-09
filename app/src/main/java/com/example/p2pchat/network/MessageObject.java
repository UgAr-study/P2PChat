package com.example.p2pchat.network;

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
    private final PublicKey from;
    private final SealedObject msg;
    private final byte[] secureMac;

    public MessageObject(PublicKey from, PublicKey to, String msg, SecretKey aesKey) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        this.from = from;
        this.msg = SymCryptography.encryptMsg(aesKey, msg);
        this.secureMac = SymCryptography.getMacMsg(aesKey, msg);
    }

    public PublicKey from() {
        return from;
    }

    public String decrypt(SecretKey aesKey) throws NoSuchPaddingException, ClassNotFoundException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        String decryptMsg = SymCryptography.decryptMsg(aesKey, msg);
        if (!Arrays.equals(SymCryptography.getMacMsg(aesKey, decryptMsg), secureMac)) {
            return null;
        } else {
            return decryptMsg;
        }
    }
}
