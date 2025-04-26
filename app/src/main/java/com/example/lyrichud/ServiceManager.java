package com.example.lyrichud;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.lyrichud.model.StatusModel;
import com.example.lyrichud.service.BleAdvertiser;
import com.example.lyrichud.service.BleScanner;
import com.example.lyrichud.service.LyricForegroundService;
import com.example.lyrichud.service.WifiHotspotManager;
import com.example.lyrichud.service.inter.OnAdvertiseStateChangeListener;
import com.example.lyrichud.service.inter.OnHotspotStateChangeListener;
import com.example.lyrichud.service.inter.OnLyricBarChangeListener;
import com.example.lyrichud.service.inter.OnPeripheralConnectListener;

public class ServiceManager {
    private final Context context;
    private final WifiHotspotManager wifiHotspotManager;
    private final BleAdvertiser bleAdvertiser;
    private final BleScanner bleScanner;

    public ServiceManager(Context context) {
        this.context = context;
        wifiHotspotManager = new WifiHotspotManager(context);
        bleAdvertiser = new BleAdvertiser(context);
        bleScanner = new BleScanner(context);

        LyricForegroundService.setStatusListener(new OnLyricBarChangeListener() {
            @Override
            public void onKugouAppConnected() {
                StatusModel.getInstance().setCatchMusicBar(true);
            }

            @Override
            public void onKugouAppDisconnected() {
                StatusModel.getInstance().setCatchMusicBar(false);
            }

            @Override
            public void onServiceStateListener(boolean state) {
                StatusModel.getInstance().setNotificationListening(state);
            }
        });

        wifiHotspotManager.setOnHotspotStateChangeListener(new OnHotspotStateChangeListener() {
            @Override
            public void onHotspotEnabled(String ssid, String password) {
                Log.d("ServiceManager", "WIFI热点启动！SSID: " + ssid + ", Password: " + password);
                // 开启 BLE 广播
                String shortCode = ssid.substring(ssid.lastIndexOf("_") + 1); // "AndroidShare_4289"->"4289"
                bleAdvertiser.startAdvertising(shortCode, password);

                StatusModel.getInstance().setHotspotOn(true);
            }

            @Override
            public void onHotspotDisabled() {
                Log.d("ServiceManager", "WIFI热点已关闭！");
                StatusModel.getInstance().setHotspotOn(false);
            }
        });

        bleAdvertiser.setOnAdvertiseStateChangeListener(new OnAdvertiseStateChangeListener() {
            @Override
            public void onAdvertiseStarted() {
                StatusModel.getInstance().setAdvertising(true);
            }

            @Override
            public void onAdvertiseStopped() {
                StatusModel.getInstance().setAdvertising(false);
            }

            @Override
            public void onAdvertiseFailed(int errorCode) {
                StatusModel.getInstance().setAdvertising(false);
            }
        });

        bleScanner.setOnPeripheralConnectListener(new OnPeripheralConnectListener() {
            @Override
            public void onPeripheralConnected(String ipAddress) {
                MainActivity.raspberry_ip = ipAddress;
                StatusModel.getInstance().setPeripheralConnected(true);
            }

            @Override
            public void onPeripheralDisconnected() {
                MainActivity.raspberry_ip = null;
                StatusModel.getInstance().setPeripheralConnected(false);
            }
        });
    }

    public void startAllServices() {
        context.startService(new Intent(context, LyricForegroundService.class));
        wifiHotspotManager.startHotspot();
        bleScanner.startScan();
    }

    public void stopAllServices() {
        context.stopService(new Intent(context, LyricForegroundService.class));
        wifiHotspotManager.stopHotspot();
        bleAdvertiser.stopAdvertising();
        bleScanner.stopScan();
    }

    // 可加 getter/setter 给 hotspotManager、bleAdvertiser等做监听器绑定

}
