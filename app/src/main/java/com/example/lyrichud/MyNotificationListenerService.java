package com.example.lyrichud;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * 通知栏监听定时脚本。轮训获取播媒体数据
 */
public class MyNotificationListenerService extends NotificationListenerService {

    public static MyNotificationListenerService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    // 在你的 MyNotificationListenerService 里
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

    }

    public StatusBarNotification[] getActiveKugouNotifications() {
        StatusBarNotification[] all = getActiveNotifications();
        return all; // 或者筛选出酷狗的
    }

}