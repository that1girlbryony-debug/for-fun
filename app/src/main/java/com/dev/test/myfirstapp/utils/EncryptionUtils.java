package com.dev.test.myfirstapp.utils;

import android.util.Base64;
import java.nio.charset.StandardCharsets;

public class EncryptionUtils {
    private static final byte[] XOR_KEY = {
        0x5A, 0x3F, 0x8C, 0x12, 0x4B, 0x7D, 0x91, 0x2E,
        0x6F, 0xA3, 0x55, 0x18, 0x9C, 0x37, 0xE2, 0x84
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