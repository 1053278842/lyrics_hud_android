package com.example.lyrichud.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

public class BleScanner {
    private final BluetoothLeScanner bleScanner;
    private final Context context;
    private final UUID SERVICE_UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB");
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("BLE", "收到扫描结果：" + result.toString());
            ScanRecord record = result.getScanRecord();
            if (record == null) {
                Log.d("BLE", "扫描记录为空");
                return;
            }

            byte[] data = record.getServiceData(new ParcelUuid(SERVICE_UUID));
            if (data != null) {
                String ip = new String(data, StandardCharsets.UTF_8);
                Log.d("BLE", "收到树莓派 IP 广播：" + ip);
            } else {
                Log.d("BLE", "没有找到匹配的服务数据");
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
        Log.d("BLE", "广播监听开始");
        // 使用正确的制造商 ID (小端序处理)
        int manufacturerId = 0x1234; // 实际应写作 0x34 0x12

        ScanFilter filter = new ScanFilter.Builder()
                .setManufacturerData(
                        manufacturerId,
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
                new ScanCallback() {
                    @Override
                    public void onScanResult(int cbType, ScanResult result) {
                        byte[] data = result.getScanRecord().getManufacturerSpecificData(manufacturerId);

                        if (data != null && data.length >= 4) {
                            // 关键解析逻辑：取前4字节
                            String ip = String.format(Locale.US, "%d.%d.%d.%d",
                                    data[0] & 0xFF, // 第一个字节转无符号
                                    data[1] & 0xFF,
                                    data[2] & 0xFF,
                                    data[3] & 0xFF);

                            Log.d("BLE", "解析到动态IP: " + ip);
                        }
                    }
                }
        );
    }

    public void stopScan() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // 权限未被授予，向用户请求权限
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    1);
            return;
        }
        bleScanner.stopScan(scanCallback);
    }
}
