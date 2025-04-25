package com.example.lyrichud.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BleAdvertiser {
    private Context context;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d("BLE", "广告开始成功");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d("BLE", "广告开始失败: " + errorCode);
        }
    };

    public BleAdvertiser(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    public void startAdvertising(String ssid, String password) {
        // 固定 UUID，确保 Python 可以匹配
        UUID serviceUuid = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB");


        // 将 WiFi SSID 和密码转为字节数组
        String wifiData = ssid + ";" + password; // 用分号分隔 SSID 和密码
        byte[] wifiDataBytes = wifiData.getBytes(StandardCharsets.UTF_8);

        // 构建 AdvertiseData
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .addServiceData(new ParcelUuid(serviceUuid), wifiDataBytes) // 使用合法的 UUID 广播数据
                .build();

        // 广播设置
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            // 权限未被授予，向用户请求权限
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.BLUETOOTH_ADVERTISE},
                    1);
            return;
        }
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseCallback);
    }

    public void stopAdvertising() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            // 权限未被授予，向用户请求权限
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.BLUETOOTH_ADVERTISE},
                    1);
            return;
        }
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }
}
