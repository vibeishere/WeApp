package com.adityasri.whatsappclone;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CipherClass {
    String AES = "AES";

    private static final String KEY = "YOUR KEY HERE";


    public String encrypt(String password){
        try {
            SecretKeySpec key = generateKey(KEY);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] encVal = cipher.doFinal(password.getBytes());
            String encryptedValue = Base64.encodeToString(encVal,Base64.DEFAULT);
            return encryptedValue;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String password){
        try {
            SecretKeySpec key = generateKey(KEY);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE,key);
            byte[] decodedVal = Base64.decode(password,Base64.DEFAULT);
            byte[] decValue = cipher.doFinal(decodedVal);
            String decryptedValue = new String(decValue);
            return decryptedValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private SecretKeySpec generateKey(String text) throws Exception {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = text.getBytes("UTF-8");
        messageDigest.update(bytes,0,bytes.length);
        byte[] key = messageDigest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key,AES);
        return secretKeySpec;
    }
}
