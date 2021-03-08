package com.example.p2pchat.security;

import android.renderscript.ScriptIntrinsicYuvToRGB;

import org.junit.Test;

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
}
