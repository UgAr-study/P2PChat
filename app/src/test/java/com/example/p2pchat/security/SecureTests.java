package com.example.p2pchat.security;

import android.renderscript.ScriptIntrinsicYuvToRGB;

import com.example.p2pchat.network.MessageObject;

import org.junit.Test;

import java.lang.reflect.Array;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Mac;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import static org.junit.Assert.*;

public class SecureTests {
    @Test
    public void Test_SymCryptography1() {
        try {
            SymCryptography symCryptography = new SymCryptography();
            SealedObject secureData = symCryptography.encryptMsg("Kek mek cheburek");
            String res = symCryptography.decryptMsg(secureData);
            assertEquals("Kek mek cheburek", res);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void Test_SymCryptographyStaticMethods() {
        try {
            String str = "Kek mek cheburek";
            String secretKeyStr = SymCryptography.generateStringSecretKey();
            SecretKey secretKey = SymCryptography.getSecretKeyByString(secretKeyStr);
            SealedObject secureObj = SymCryptography.encryptMsg(secretKey, str);
            String res = SymCryptography.decryptMsg(secretKey, secureObj);
            assertEquals(res, str);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void Test_SymCryptographyGson() {
        try {
            String str = "Kek mek cheburek";
            String pwd = "kek";
            String encryptMsg = SymCryptography.encryptByPwdGson(str, pwd);
            String decryptMsg = SymCryptography.decryptByPwdGson(encryptMsg, pwd);
            assertEquals(str, decryptMsg);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void Test_AsymCryptography() {
        try {
            String str = "Kek mek cheburek";
            String pwd = "kek";
            AsymCryptography asymCryptography = new AsymCryptography();
            SealedObject sealedObject = asymCryptography.encryptMsg(str);
            String res = asymCryptography.decryptMsg(sealedObject);
            assertEquals(res, str);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void Test_GetPubKeyFromPrivKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = AsymCryptography.getPublicKeyFromPrivateKey(keyPair.getPrivate());
            assertEquals(publicKey, keyPair.getPublic());
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void Test_AESKEYMSG() {
        try {
            AsymCryptography user = new AsymCryptography();
            SymCryptography aesKey = new SymCryptography();
            String pubKey = AsymCryptography.getStringAsymKey(user.getPublicKey());
            String aesKeyString = SymCryptography.getStringKey(aesKey.getSecretKey());
            MessageObject aesMsg = new MessageObject("user", aesKeyString,
                                                     pubKey,
                                                     MessageObject.SEND_AES_KEY);
            String privateKey = AsymCryptography.getStringAsymKey(user.getPrivateKey());
            assertEquals(aesMsg.decryptAesMsg(privateKey),
                         aesKeyString);
        } catch (Exception e) {
            e.getStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void Test_getPubKeyFromPrivate() {
        try{
            AsymCryptography asymCryptography = new AsymCryptography();
            PrivateKey privateKey = asymCryptography.getPrivateKey();
            assertEquals(asymCryptography.getPublicKey(), AsymCryptography.getPublicKeyFromPrivateKey(privateKey));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void Test_getMac() {
        try {
            AsymCryptography asymCryptography = new AsymCryptography();
            PublicKey publicKey = asymCryptography.getPublicKey();
            String msg = "User";
            assertEquals(SymCryptography.getMacMsg(publicKey, msg), SymCryptography.getMacMsg(publicKey, msg));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void Test_generateAesKeyByString() {
        try {
            String key = "adasd";
            String hash = SymCryptography.getStringHash(key);

            assertEquals(
                    SymCryptography.getStringHash(key),
                    SymCryptography.getStringHash(key)
            );

            SecretKey secretKey = SymCryptography.generateAESKeyByPwd(hash);
            assertEquals(
                    SymCryptography.generateAESKeyByPwd(hash),
                    SymCryptography.generateAESKeyByPwd(hash)
            );

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);


        } catch (Exception e) {
            assertTrue(false);
        }
    }

}
