package com.fardin.callbomber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private Button buttonStartBombing;
    private Button buttonStopBombing;
    private static final int CALL_PERMISSION_CODE = 101;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isBombing = false;
    private int callCount = 0;
    private final int MAX_CALLS = 500;   // সর্বোচ্চ ৫০০টা কল

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonStartBombing = findViewById(R.id.buttonStartBombing);
        buttonStopBombing = findViewById(R.id.buttonStopBombing);

        buttonStartBombing.setOnClickListener(v -> checkPermissionAndStart());
        buttonStopBombing.setOnClickListener(v -> stopBombing());
    }

    private void checkPermissionAndStart() {
        if (isBombing) {
            Toast.makeText(this, "এখনই চলছে...", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = editTextPhoneNumber.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(this, "ফোন নাম্বার লিখুন", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CALL_PHONE}, 
                    CALL_PERMISSION_CODE);
        } else {
            startBombing(phone);
        }
    }

    private void startBombing(String phoneNumber) {
        isBombing = true;
        callCount = 0;

        Toast.makeText(this, "Bombing শুরু হচ্ছে... (Max 50)", Toast.LENGTH_LONG).show();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isBombing || callCount >= MAX_CALLS) {
                    stopBombing();
                    return;
                }

                makeCall(phoneNumber);
                callCount++;

                // ১ সেকেন্ড পর পর কল
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void makeCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "কল করতে সমস্যা", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopBombing() {
        isBombing = false;
        handler.removeCallbacksAndMessages(null);
        Toast.makeText(this, "Bombing বন্ধ হয়েছে। মোট কল: " + callCount, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String phone = editTextPhoneNumber.getText().toString().trim();
                if (!phone.isEmpty()) startBombing(phone);
            } else {
                Toast.makeText(this, "কল করার অনুমতি দিতে হবে", Toast.LENGTH_LONG).show();
            }
        }
    }
}
