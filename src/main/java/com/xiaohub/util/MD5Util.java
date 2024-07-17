package com.xiaohub.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * 将输入的字符串转换为MD5哈希。
     *
     * @param input 要加密的字符串
     * @return 加密后的字符串
     */
    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            return convertByteToHex(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 将字节数据转换为十六进制字符串。
     *
     * @param byteData 字节数据
     * @return 十六进制字符串
     */
    private static String convertByteToHex(byte[] byteData) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            stringBuilder.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
//        String myPassword = "20240717002101916The quick brown fox jumped over the lazy dog.a32s1a3s123a1s32a1s653Q4Pe5uW5AaKOBFVkM1fP";
//        String myPasswordMD5 = getMD5(myPassword);
//        System.out.println("Original: " + myPassword);
//        System.out.println("MD5 Encoded: " + myPasswordMD5);

    }
}

