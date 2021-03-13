package com.example.p2pchat.security;

//import com.google.gson.Gson;

import com.google.gson.Gson;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


public class SymCryptography {
    private SecretKey secretKey = null;

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public SymCryptography() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        secretKey = keyGenerator.generateKey();
    }

    public SealedObject encryptMsg(String msg) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return new SealedObject(msg, cipher);
    }

    static public SealedObject encryptMsg(SecretKey key, String msg) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new SealedObject(msg, cipher);
        } catch (Exception e) {
            return null;
        }
    }

    static public String decryptMsg(SecretKey key, SealedObject data) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, IOException, ClassNotFoundException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return data.getObject(cipher).toString();
    }

    static public SecretKey getSecretKeyByString(String secKey) {
        byte[] decodedKey = Base64.getDecoder().decode(secKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return originalKey;
    }

    static public String generateStringSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            return encodedKey;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public String decryptMsg(SealedObject data) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, IOException, ClassNotFoundException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return data.getObject(cipher).toString();
    }

    public byte[] getMacMsg(String msg) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] data = msg.getBytes("UTF-8");
        return mac.doFinal(data);
    }

    static public byte[] getMacMsg( SecretKey key, String msg) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        byte[] data = msg.getBytes("UTF-8");
        return mac.doFinal(data);
    }

    static public byte[] getMacMsg(PublicKey key, String msg) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        byte[] data = msg.getBytes("UTF-8");
        return mac.doFinal(data);
    }

    static public SealedObject encryptByPwd(String msg, String pwd) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, InvalidKeySpecException {
        SecretKey key = generateAESKeyByPwd(pwd);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new SealedObject(msg, cipher);
    }

    static public String encryptByPwdGson(String msg, String pwd) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, InvalidKeySpecException {
        SealedObject encryptMsg = SymCryptography.encryptByPwd(msg, pwd);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        out = new ObjectOutputStream(bos);
        out.writeObject(encryptMsg);
        out.flush();
        byte[] bytes = bos.toByteArray();
        Gson gson = new Gson();
        String json = gson.toJson(bytes);
        return json;
    }

    static public String decryptByPwd(SealedObject data, String pwd) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, InvalidKeySpecException {
        SecretKey key = generateAESKeyByPwd(pwd);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return (String) data.getObject(cipher);
    }

    static public String decryptByPwdGson(String data, String pwd) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, InvalidKeySpecException {
        Gson gson = new Gson();
        byte[] json = gson.fromJson(data, byte[].class);
        ByteArrayInputStream bis = new ByteArrayInputStream(json);
        ObjectInput in = null;
        in = new ObjectInputStream(bis);
        SealedObject sobj = (SealedObject) in.readObject();
        return SymCryptography.decryptByPwd(sobj, pwd);
    }

    private static SecretKey generateAESKeyByPwd(String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(pwd.getBytes());
        byte[] encodedhash = digest.digest(pwd.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(encodedhash, 0, encodedhash.length, "AES");
    }

    public static String getStringHash(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}