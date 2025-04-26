package com.example.bluetoothgatt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.UUID;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private static final UUID SERVICE_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    private static final UUID CALL_CHARACTERISTIC_UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
    private static final UUID SMS_CHARACTERISTIC_UUID = UUID.fromString("0000FFE2-0000-1000-8000-00805F9B34FB");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic callCharacteristic;
    private BluetoothGattCharacteristic smsCharacteristic;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        setupGattServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startAdvertising();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAdvertising();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupGattServer() {
        // Create GATT service
        gattService = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Create call characteristic
        callCharacteristic = new BluetoothGattCharacteristic(
                CALL_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        // Create SMS characteristic
        smsCharacteristic = new BluetoothGattCharacteristic(
                SMS_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        // Add descriptors for notifications
        BluetoothGattDescriptor callDescriptor = new BluetoothGattDescriptor(
                CLIENT_CHARACTERISTIC_CONFIG,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE
        );
        callCharacteristic.addDescriptor(callDescriptor);

        BluetoothGattDescriptor smsDescriptor = new BluetoothGattDescriptor(
                CLIENT_CHARACTERISTIC_CONFIG,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE
        );
        smsCharacteristic.addDescriptor(smsDescriptor);

        // Add characteristics to service
        gattService.addCharacteristic(callCharacteristic);
        gattService.addCharacteristic(smsCharacteristic);
    }

    public void startAdvertising() {
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "BluetoothLeAdvertiser is null");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();

        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);
    }

    public void stopAdvertising() {
        if (bluetoothLeAdvertiser != null) {
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        }
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e(TAG, "LE Advertise Failed: " + errorCode);
        }
    };

    public void sendCallNotification(String callInfo) {
        if (callCharacteristic != null) {
            callCharacteristic.setValue(callInfo.getBytes());
            // Notify connected clients
            // This would be implemented in the GATT server callback
        }
    }

    public void sendSMSNotification(String smsInfo) {
        if (smsCharacteristic != null) {
            smsCharacteristic.setValue(smsInfo.getBytes());
            // Notify connected clients
            // This would be implemented in the GATT server callback
        }
    }

    public void handleCommand(String command) {
        if (command.equals("ANSWER")) {
            // Handle call answer
            Log.d(TAG, "Call answered");
        } else if (command.equals("REJECT")) {
            // Handle call reject
            Log.d(TAG, "Call rejected");
        }
    }
} 