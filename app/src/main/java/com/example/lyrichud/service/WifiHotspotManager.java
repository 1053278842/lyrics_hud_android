package com.example.lyrichud.service;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.lyrichud.service.inter.OnHotspotStateChangeListener;

public class WifiHotspotManager {

    private Context context;
    private WifiManager wifiManager;
    private LocalOnlyHotspotReservation currentHotspot;
    private OnHotspotStateChangeListener hotspotStateChangeListener;


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

                if (hotspotStateChangeListener != null) {
                    hotspotStateChangeListener.onHotspotEnabled(ssid, password);
                }

            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Toast.makeText(context, "启动热点失败", Toast.LENGTH_SHORT).show();
            }
        }, null);
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

            if (hotspotStateChangeListener != null) {
                hotspotStateChangeListener.onHotspotDisabled();
            }
        }
    }

    public void setOnHotspotStateChangeListener(OnHotspotStateChangeListener listener) {
        this.hotspotStateChangeListener = listener;
    }


}
