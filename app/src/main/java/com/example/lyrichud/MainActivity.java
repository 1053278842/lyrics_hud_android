package com.example.lyrichud;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.lyrichud.model.StatusModel;
import com.example.lyrichud.ui.StatusUpdater;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {
    public static String raspberry_ip = "127.0.0.1";
    private final String OPEN_TEXT = "关闭服务";
    private final String CLOSE_TEXT = "开启服务";
    private MaterialCardView toggleButton;
    private boolean isOn = false;
    private ServiceManager serviceManager;

    private StatusUpdater statusUpdater;

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
        // startService(new Intent(this, LyricForegroundService.class));

        // 启动 Python
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // 重启 NotificationListenerService 确保监听生效.确保授权后能及时监听到数据
//        new Handler().postDelayed(this::toggleNotificationListenerService, 1000);

        // 服务管理
        serviceManager = new ServiceManager(this);

        // UI方面 设置按钮监听器
        toggleButton = findViewById(R.id.toggle_button_bg);
        toggleButton.setOnClickListener(view -> toggleService());

        // 状态组件绑定到状态ui管理器中
        statusUpdater = new StatusUpdater(this);
        statusUpdater.update();
        // 将 UI 更新模块 与 状态对象变更回调 绑定
        StatusModel.getInstance().setOnStatusChangeListener(new StatusModel.OnStatusChangeListener() {
            @Override
            public void onStatusChanged() {
                statusUpdater.update();
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void toggleService() {
        isOn = !isOn;
        if (isOn) {
            serviceManager.startAllServices();
            TextView text_title = findViewById(R.id.toggle_button_title);
            text_title.setText(getString(R.string.button_status_running));
            TextView text_summary = findViewById(R.id.toggle_button_summary);
            text_summary.setText(getString(R.string.button_status_click_to_stop));
            ImageView image_ico = findViewById(R.id.toggle_button_img);
            image_ico.setImageResource(R.drawable.ic_clash_started);

            MaterialCardView bg = findViewById(R.id.toggle_button_bg);
            bg.setCardBackgroundColor(getColor(R.color.colorAccent));
//            toggleButton.setText(OPEN_TEXT);
        } else {
            serviceManager.stopAllServices();
//            toggleButton.setText(CLOSE_TEXT);
            TextView text_title = findViewById(R.id.toggle_button_title);
            text_title.setText(getString(R.string.button_status_stopped));
            TextView text_summary = findViewById(R.id.toggle_button_summary);
            text_summary.setText(getString(R.string.button_status_click_to_start));
            ImageView image_ico = findViewById(R.id.toggle_button_img);
            image_ico.setImageResource(R.drawable.ic_clash_stopped);

            MaterialCardView bg = findViewById(R.id.toggle_button_bg);
            bg.setCardBackgroundColor(getColor(R.color.gray));
        }
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
        // 重启监听服务后，更新状态
        StatusModel.getInstance().setNotificationListening(true);
        if (statusUpdater != null) {
            statusUpdater.update();
        }
    }

}