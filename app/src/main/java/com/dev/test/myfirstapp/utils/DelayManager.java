package com.dev.test.myfirstapp.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.dev.test.myfirstapp.ui.MainActivity;
import java.util.Random;

public class DelayManager {
    private static final String PREFS_NAME = "app_prefs";
    private static final long MIN_DELAY = 2 * 60 * 60 * 1000;
    private static final long MAX_DELAY = 6 * 60 * 60 * 1000;

    public static long calculateActivationDelay() {
        Random random = new Random();
        long randomDelay = MIN_DELAY + (long)(random.nextDouble() * (MAX_DELAY - MIN_DELAY));
        long jitter = random.nextInt(600000);
        return randomDelay + jitter;
    }

    public static void schedulePermissionRetry(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int retryCount = prefs.getInt("perm_retry_count", 0);
        if (retryCount >= 3) return;

        prefs.edit().putInt("perm_retry_count", retryCount + 1).apply();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long retryDelay = (retryCount + 1) * 24 * 60 * 60 * 1000L;
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + retryDelay, pendingIntent);
    }
}