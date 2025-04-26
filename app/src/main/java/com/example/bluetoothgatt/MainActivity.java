package com.example.bluetoothgatt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    
    private MaterialButton startButton;
    private MaterialButton stopButton;
    private TextView statusText;
    private TextView deviceNameText;
    private TextView serviceStatusText;
    private TextView connectedDevicesText;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        statusText = findViewById(R.id.status_text);
        deviceNameText = findViewById(R.id.device_name_text);
        serviceStatusText = findViewById(R.id.service_status_text);
        connectedDevicesText = findViewById(R.id.connected_devices_text);

        // Initialize Bluetooth adapter
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Set device name
        if (bluetoothAdapter != null) {
            deviceNameText.setText("Device Name: " + bluetoothAdapter.getName());
        }

        startButton.setOnClickListener(v -> startBluetoothService());
        stopButton.setOnClickListener(v -> stopBluetoothService());

        // Check and request permissions
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_SMS);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                    permissions.toArray(new String[0]), 
                    REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "All permissions are required for the app to work properly", 
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
        }
    }

    private void startBluetoothService() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }

        Intent serviceIntent = new Intent(this, BluetoothService.class);
        startService(serviceIntent);
        
        // Update UI
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusText.setText("Status: Advertising");
        statusText.setTextColor(getResources().getColor(R.color.success));
        serviceStatusText.setText("Service Status: Advertising");
        Toast.makeText(this, "Bluetooth service started", Toast.LENGTH_SHORT).show();
    }

    private void stopBluetoothService() {
        Intent serviceIntent = new Intent(this, BluetoothService.class);
        stopService(serviceIntent);
        
        // Update UI
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusText.setText("Status: Not Connected");
        statusText.setTextColor(getResources().getColor(R.color.text_secondary));
        serviceStatusText.setText("Service Status: Not Advertising");
        Toast.makeText(this, "Bluetooth service stopped", Toast.LENGTH_SHORT).show();
    }
} 