package com.xiaohub.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM_1 = "AES";
    private static final String ALGORITHM_2 = "SHA1PRNG";
    private static final int KEY_SIZE = 128;

    private static final KeyGenerator keyGenerator;

    static {
        try {
            keyGenerator = KeyGenerator.getInstance(ALGORITHM_1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(String key, String data) throws Exception {
        SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM_2);
        secureRandom.setSeed(key.getBytes());
        keyGenerator.init(KEY_SIZE, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM_1);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encVal = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encVal);
    }

    public static String decrypt(String key, String encryptedData) throws Exception {
        SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM_2);
        secureRandom.setSeed(key.getBytes());
        keyGenerator.init(KEY_SIZE, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM_1);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = cipher.doFinal(decodedValue);
        return new String(decValue);
    }

}


