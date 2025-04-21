package com.example.lyrichud;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class PermissionHelper {

    private static final String PREFS_NAME = "PermissionPrefs";
    private static final String KEY_AUTO_START_SHOWN = "autoStartDialogShown";
    private static final String KEY_BATTERY_DIALOG_SHOWN = "batteryDialogShown";


    public static void checkAutoStartPermission(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_AUTO_START_SHOWN, false)) {
            new AlertDialog.Builder(context)
                    .setTitle("开启自启动权限")
                    .setMessage("为了保证歌词服务正常运行，请允许应用的自启动权限。")
                    .setPositiveButton("去设置", (dialog, which) -> {
                        openAutoStartSettings(context);
                        prefs.edit().putBoolean(KEY_AUTO_START_SHOWN, true).apply();
                    })
                    .setNegativeButton("暂不设置", null)
                    .show();
        }
    }

    public static void checkBatteryOptimization(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_BATTERY_DIALOG_SHOWN, false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new AlertDialog.Builder(context)
                    .setTitle("关闭电池优化")
                    .setMessage("请将应用的电池优化策略设置为“无限制”，以确保歌词服务在后台持续运行。")
                    .setPositiveButton("去设置", (dialog, which) -> {
                        context.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                        prefs.edit().putBoolean(KEY_BATTERY_DIALOG_SHOWN, true).apply();
                    })
                    .setNegativeButton("暂不设置", null)
                    .show();
        }
    }

    public static boolean isNotificationServiceEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(pkgName);
    }

    public static void requestNotificationServicePermission(Context context) {
        if (!isNotificationServiceEnabled(context)) {
            Toast.makeText(context, "请开启通知监听权限", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }

    public static void openAutoStartSettings(Context context) {
        Intent intent = new Intent();
        String manufacturer = Build.MANUFACTURER.toLowerCase();

        if (manufacturer.contains("xiaomi")) {
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        } else if (manufacturer.contains("huawei")) {
            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
        } else if (manufacturer.contains("oppo")) {
            intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.autoboot.AutoBootManageActivity"));
        } else if (manufacturer.contains("vivo")) {
            intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"));
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // 如果跳转失败，打开应用详情页
            Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            fallback.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(fallback);
        }
    }
}
