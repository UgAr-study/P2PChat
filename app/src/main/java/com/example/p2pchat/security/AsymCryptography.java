package com.example.p2pchat.security;

import android.content.SharedPreferences;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.Cipher;


import java.io.IOException;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;

public class AsymCryptography {
    public final static String KEY_STORE_NAME = "keyStrore";
    private final static String PRIVATE_KEY = "Private_Key";
    private PrivateKey privateKey = null;

    static public PublicKey generateNewPair(String pwd, SharedPreferences keyStore) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String encryptKeyString = SymCryptography.encryptByPwdGson(keyPair.getPrivate().toString(), pwd);
            SharedPreferences.Editor editor = keyStore.edit();
            editor.putString(PRIVATE_KEY, encryptKeyString);
            editor.apply();
            return keyPair.getPublic();
        } catch (Exception e) {
            return null;
        }
    }

    static public PrivateKey getPrivateKeyFromString(String keyStr) {
        try {
            byte[] data = Base64.getDecoder().decode((keyStr.getBytes()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            return fact.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    static public PublicKey getPublicKeyFromString(String keyStr) {

        try {
            byte[] data = Base64.getDecoder().decode((keyStr.getBytes(StandardCharsets.UTF_8)));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            return fact.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    private BigInteger readModulusFromString(String keyData) {
        int begin = keyData.lastIndexOf("modulus");
        int end = keyData.indexOf('\n', begin);
        String modulusStr = keyData.substring(begin + 9, end);
        return new BigInteger(modulusStr);
    }

    static public String getStringAsymKey(PublicKey pubKey) {
        byte [] byte_pubkey = pubKey.getEncoded();
        return Base64.getEncoder().encodeToString(byte_pubkey);
    }

    private BigInteger readExponentFromString(String keyData) {
        int begin = keyData.lastIndexOf("exponent:");
        int end = keyData.indexOf('\n', begin);
        String modulusStr = keyData.substring(begin + 10);
        return new BigInteger(modulusStr);
    }

    public void loadPrivateKey(String pwd, SharedPreferences keyStore) throws NoSuchPaddingException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, ClassNotFoundException {
        String encryptKeyData = keyStore.getString(PRIVATE_KEY, null);
        String keyData = SymCryptography.decryptByPwdGson(encryptKeyData, pwd);

        BigInteger modulus = readModulusFromString(keyData);
        BigInteger prExponent = readExponentFromString(keyData);
        RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(modulus, prExponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        privateKey = factory.generatePrivate(privateSpec);
    }

    static public SealedObject encryptMsg(String msg, PublicKey publicKey) {
        try {
            Cipher encrypt=Cipher.getInstance("RSA");
            encrypt.init(Cipher.ENCRYPT_MODE, publicKey);
            return new SealedObject( msg, encrypt);
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | IOException | InvalidKeyException e) {
            return null;
        }
    }

    public String decryptMsg(SealedObject encryptMsg) {
        try {
            Cipher decrypt = Cipher.getInstance("RSA");
            decrypt.init(Cipher.DECRYPT_MODE, privateKey);
            return (String) encryptMsg.getObject(decrypt);
        }

        catch (Exception e) {
            return null;
        }
    }
}