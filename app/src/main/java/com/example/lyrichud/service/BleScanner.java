package com.example.lyrichud.service;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.lyrichud.PermissionHelper;
import com.example.lyrichud.service.inter.OnPeripheralConnectListener;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

public class BleScanner {
    private final BluetoothLeScanner bleScanner;
    private final Context context;
    private final int MANUFACTURER_ID = 0x1234; // 实际应写作 0x34 0x12
    private String current_ip;
    private OnPeripheralConnectListener peripheralConnectListener;
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int cbType, ScanResult result) {
            byte[] data = result.getScanRecord().getManufacturerSpecificData(MANUFACTURER_ID);

            if (data != null && data.length >= 4) {
                // 关键解析逻辑：取前4字节
                String ip = String.format(Locale.US, "%d.%d.%d.%d",
                        data[0] & 0xFF, // 第一个字节转无符号
                        data[1] & 0xFF,
                        data[2] & 0xFF,
                        data[3] & 0xFF);

                if (!Objects.equals(current_ip, ip)) {
//                    if ("0.0.0.0".equals(current_ip)) {
//                        return;
//                    }
                    Log.d("BLE", "找到IP：" + ip);
                    current_ip = ip;
                    if (peripheralConnectListener != null) {
                        peripheralConnectListener.onPeripheralConnected(ip);
                    }
                }
            }
        }
    };

    public BleScanner(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();

    }

    public void startScan() {
        if (!PermissionHelper.requestBluetoothEnabled(context)) {
            return; // 如果未开启蓝牙，则退出
        }
        Log.d("BLE", "广播监听开始");
        ScanFilter filter = new ScanFilter.Builder()
                .setManufacturerData(
                        MANUFACTURER_ID,
                        null, // 匹配指定 IP
                        null // 掩码
                )
                .build();

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build();


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // 权限未被授予，向用户请求权限
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    1);
            return;
        }


        bleScanner.startScan(
                Collections.singletonList(filter),
                settings,
                scanCallback
        );
    }

    public void stopScan() {
        if (!PermissionHelper.requestBluetoothEnabled(context)) {
            return; // 如果未开启蓝牙，则退出
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // 权限未被授予，向用户请求权限
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    1);
            return;
        }
        bleScanner.stopScan(scanCallback);
    }


    public void setOnPeripheralConnectListener(OnPeripheralConnectListener listener) {
        this.peripheralConnectListener = listener;
    }
}
