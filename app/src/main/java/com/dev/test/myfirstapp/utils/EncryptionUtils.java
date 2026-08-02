package com.dev.test.myfirstapp.utils;

import android.util.Base64;
import java.nio.charset.StandardCharsets;

public class EncryptionUtils {
    private static final byte[] XOR_KEY = {
        (byte) 0x5A, (byte) 0x3F, (byte) 0x8C, (byte) 0x12, 
        (byte) 0x4B, (byte) 0x7D, (byte) 0x91, (byte) 0x2E,
        (byte) 0x6F, (byte) 0xA3, (byte) 0x55, (byte) 0x18, 
        (byte) 0x9C, (byte) 0x37, (byte) 0xE2, (byte) 0x84
    };

    public static String decryptString(String encryptedBase64) {
        try {
            byte[] encryptedData = Base64.decode(encryptedBase64, Base64.DEFAULT);
            byte[] decryptedData = xorDecrypt(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) { return ""; }
    }

    public static String encryptString(String plaintext) {
        byte[] plainData = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedData = xorEncrypt(plainData);
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
    }

    private static byte[] xorEncrypt(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ XOR_KEY[i % XOR_KEY.length]);
        }
        return result;
    }

    private static byte[] xorDecrypt(byte[] data) { return xorEncrypt(data); }
}
