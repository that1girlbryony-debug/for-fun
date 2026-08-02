package com.dev.test.myfirstapp;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import java.lang.reflect.Method;

public class AppController extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                Method setHiddenApiExemptions = StrictMode.class
                    .getDeclaredMethod("setHiddenApiExemptions", String[].class);
                setHiddenApiExemptions.invoke(null, (Object) new String[]{"L"});
            } catch (Exception ignored) {}
        }
    }

    public static Context getContext() { return appContext; }
}