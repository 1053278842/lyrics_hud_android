package com.example.lyrichud;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.lyrichud.ble.BleAdvertiser;
import com.example.lyrichud.ble.BleScanner;

public class MainActivity extends AppCompatActivity {
    private final String OPEN_TEXT = "关闭服务";
    private final String CLOSE_TEXT = "开启服务";
    private Button toggleButton;
    private boolean isOn = false;
    private WifiHotspotManager wifiHotspotManager;
    private BleAdvertiser bleAdvertiser;
    private BleScanner bleScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Main", "进入MainActivity!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 【权限检测】 自启动
        PermissionHelper.checkAutoStartPermission(this);
        // 【权限检测】 省电策略
        PermissionHelper.checkBatteryOptimization(this);
        // 【权限检测】 监听通知（通知状态栏）
        PermissionHelper.requestNotificationServicePermission(this);
        // 【权限检测】 WIFI相关
        PermissionHelper.requestWifiPermission(this);
        // 【权限检测】 BLE相关：蓝牙
        PermissionHelper.requestBluetoothPermission(this);
        // 【权限检测】 BLE相关：定位
        PermissionHelper.requestLocationPermission(this);

        // 启动前台服务持续获取媒体信息
        startService(new Intent(this, LyricForegroundService.class));

        // 启动 Python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // 重启 NotificationListenerService 确保监听生效
        new Handler().postDelayed(this::toggleNotificationListenerService, 1000);


        // UI方面

        // 初始化 WifiHotspotManager
        wifiHotspotManager = new WifiHotspotManager(this);
        wifiHotspotManager.setOnHotspotStartedListener((ssid, password) -> {
            Log.d("Hotspot", "SSID: " + ssid + ", Password: " + password);
            // 开启 BLE 广播
            String shortCode = ssid.substring(ssid.lastIndexOf("_") + 1); // "AndroidShare_4289"->"4289"

            bleAdvertiser.startAdvertising(shortCode, password);
        });
        // 初始化 ble
        bleAdvertiser = new BleAdvertiser(this);
        bleScanner = new BleScanner(this);
        // 设置按钮监听器
        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOn = !isOn;
                if (isOn) {
                    toggleButton.setText(OPEN_TEXT);
                    // 启动 状态栏监听
                    startService(new Intent(MainActivity.this, LyricForegroundService.class));
                    // 启动 Wi-Fi 热点
                    wifiHotspotManager.startHotspot();

                    // 开始扫描树莓派的 IP 广播
                    bleScanner.startScan();

                } else {
                    toggleButton.setText(CLOSE_TEXT);
                    // 关闭 状态栏监听
                    stopService(new Intent(MainActivity.this, LyricForegroundService.class));
                    // 停止 Wi-Fi 热点
                    wifiHotspotManager.stopHotspot();

                    // 停止 BLE 广播与扫描
                    bleAdvertiser.stopAdvertising();
                    bleScanner.stopScan();
                }
            }
        });
    }

    // 用 setComponentEnabledSetting 重启服务
    private void toggleNotificationListenerService() {
        ComponentName thisComponent = new ComponentName(this, MyNotificationListenerService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}