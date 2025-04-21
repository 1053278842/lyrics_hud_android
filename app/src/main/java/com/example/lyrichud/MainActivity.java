package com.example.lyrichud;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

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

        // 启动前台服务持续获取媒体信息
        startService(new Intent(this, LyricForegroundService.class));

        // 启动 Python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // 重启 NotificationListenerService 确保监听生效
        new Handler().postDelayed(this::toggleNotificationListenerService, 1000);
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