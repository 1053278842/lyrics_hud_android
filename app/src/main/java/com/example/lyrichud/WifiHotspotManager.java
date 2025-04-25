package com.example.lyrichud;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.lyrichud.inter.OnHotspotStartedListener;

public class WifiHotspotManager {

    private Context context;
    private WifiManager wifiManager;
    private LocalOnlyHotspotReservation currentHotspot;
    private OnHotspotStartedListener hotspotStartedListener;

    public WifiHotspotManager(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    // 启动 Wi-Fi 热点
    public void startHotspot() {
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            Toast.makeText(context, "请启用 Wi-Fi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查并请求权限
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 权限未被授予，向用户请求权限
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                return;
            }
        }
        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
            @Override
            public void onStarted(LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                currentHotspot = reservation;
                String ssid = reservation.getWifiConfiguration().SSID;
                String password = reservation.getWifiConfiguration().preSharedKey;
                Toast.makeText(context, "热点已启动: " + ssid, Toast.LENGTH_SHORT).show();

                if (hotspotStartedListener != null) {
                    hotspotStartedListener.onStarted(ssid, password);
                }

                // 获取当前连接的设备 IP 地址
                getConnectedDeviceIp();
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Toast.makeText(context, "启动热点失败", Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    // 获取当前连接的设备 IP 地址
    private void getConnectedDeviceIp() {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();

            @SuppressLint("DefaultLocale") String ip = String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff));

            Toast.makeText(context, "当前连接设备的 IP 地址: " + ip, Toast.LENGTH_SHORT).show();
        }
    }

    // 释放热点
    public void stopHotspot() {
        if (currentHotspot != null) {
            Toast.makeText(context, "热点已经关闭！", Toast.LENGTH_SHORT).show();
            
            // 停止热点
            if (currentHotspot != null) {
                currentHotspot.close();
                currentHotspot = null; // 确保释放引用
            }
        }
    }

    public void setOnHotspotStartedListener(OnHotspotStartedListener listener) {
        this.hotspotStartedListener = listener;
    }

}
