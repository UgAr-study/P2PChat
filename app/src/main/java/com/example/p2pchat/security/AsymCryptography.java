package com.example.p2pchat.security;

import android.content.SharedPreferences;
import android.util.Log;

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
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;

public class AsymCryptography {
    public final static String KEY_STORE_NAME = "keyStrore";
    private final static String PRIVATE_KEY = "Private_Key";
    private PrivateKey privateKey = null;
    private PublicKey publicKey = null;

    private static SharedPreferences keyStore = null;

    public AsymCryptography() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            return;
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    static public SharedPreferences getKeyStore() {
        return keyStore;
    }

    //*Load private key
    public AsymCryptography(String pwd, SharedPreferences keyStore) {
        try {
            String encryptKeyData = keyStore.getString(PRIVATE_KEY, null);
            String privateKeyString = SymCryptography.decryptByPwdGson(encryptKeyData, pwd);
            privateKey = AsymCryptography.getPrivateKeyFromString(privateKeyString);
            publicKey = AsymCryptography.getPublicKeyFromPrivateKey(privateKey);
        } catch (Exception e) {
            privateKey = null;
            publicKey = null;
            return;
        }
    }

    static public String loadPrivateKeyFromKeyStore(String pwd, SharedPreferences keyStore) {
        try {
            String encryptKeyData = keyStore.getString(PRIVATE_KEY, null);
            String privateKeyString = SymCryptography.decryptByPwdGson(encryptKeyData, pwd);
            PrivateKey privKey = AsymCryptography.getPrivateKeyFromString(privateKeyString);
            return getStringAsymKey(privKey);
        } catch (Exception e) {
            Log.e("MyTag|AsymCrypto", "load private key error");
            return null;
        }
    }

    static public void setKeyStore(SharedPreferences kStore) {
        keyStore = kStore;
    }

    static public PublicKey generateAndSaveNewPair(String pwd, SharedPreferences newKeyStore) {
        try {
            keyStore = newKeyStore;
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String encryptKeyString = SymCryptography.encryptByPwdGson(AsymCryptography.getStringAsymKey(keyPair.getPrivate()), pwd);
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
            byte [] pkcs8EncodedBytes = Base64.getDecoder().decode(keyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            return fact.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e("MyLog|AsymCrypto", "error in getPrivateKeyFromString");
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

    private static BigInteger readModulusFromString(String keyData) {
        int begin = keyData.lastIndexOf("modulus");
        int end = keyData.indexOf(',', begin);
        String modulusStr = keyData.substring(begin + 9, end);
        return new BigInteger(modulusStr);
    }

    static public String getStringAsymKey(PublicKey pubKey) {
        byte [] byte_pubkey = pubKey.getEncoded();
        return Base64.getEncoder().encodeToString(byte_pubkey);
    }

    static public String getStringAsymKey(PrivateKey privateKey) {
        byte [] byte_private_key = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(byte_private_key);
    }

    private static BigInteger readExponentFromString(String keyData) {
        int begin = keyData.lastIndexOf("exponent:");
        int end = keyData.indexOf('\n', begin);
        String modulusStr = keyData.substring(begin + 10);
        return new BigInteger(modulusStr);
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

    public SealedObject encryptMsg(String msg) {
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

    static public String decryptMsg(SealedObject encryptMsg, PrivateKey privKey) {
        try {
            Cipher decrypt = Cipher.getInstance("RSA");
            decrypt.init(Cipher.DECRYPT_MODE, privKey);
            return (String) encryptMsg.getObject(decrypt);
        } catch (Exception e) {
            return null;
        }
    }

    static public PublicKey getPublicKeyFromPrivateKey(PrivateKey privateKey) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec priv = kf.getKeySpec(privateKey, RSAPrivateKeySpec.class);

            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(priv.getModulus(), BigInteger.valueOf(65537));

            return kf.generatePublic(keySpec);
        } catch (Exception e) {
            Log.d("myTagSecure", "getPublicKeyFromPrivateKey fall");
            return null;
        }
    }
}