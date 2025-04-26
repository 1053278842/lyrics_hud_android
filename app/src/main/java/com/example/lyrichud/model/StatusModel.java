package com.example.lyrichud.model;

public class StatusModel {
    private static final StatusModel instance = new StatusModel();

    // 是否启动通知栏监听
    private boolean isNotificationListening = false;
    // 是否监听到音乐bar
    private boolean isCatchMusicBar = false;
    // 是否启动wifi
    private boolean isHotspotOn = false;
    // 是否开始广播数据
    private boolean isAdvertising = false;
    // 是否被连接外设（wifi连接）
    private boolean isPeripheralConnected = false;
    private OnStatusChangeListener statusChangeListener;

    private StatusModel() {
    }

    public static StatusModel getInstance() {
        return instance;
    }

    public boolean isHotspotOn() {
        return isHotspotOn;
    }

    public void setHotspotOn(boolean hotspotOn) {
        isHotspotOn = hotspotOn;
        notifyChange();
    }

    public boolean isAdvertising() {
        return isAdvertising;
    }

    public void setAdvertising(boolean advertising) {
        isAdvertising = advertising;
        notifyChange();
    }

    public boolean isPeripheralConnected() {
        return isPeripheralConnected;
    }

    public void setPeripheralConnected(boolean peripheralConnected) {
        isPeripheralConnected = peripheralConnected;
        notifyChange();
    }

    public boolean isNotificationListening() {
        return isNotificationListening;
    }

    public void setNotificationListening(boolean notificationListening) {
        isNotificationListening = notificationListening;
        notifyChange();
    }

    public boolean isCatchMusicBar() {
        return isCatchMusicBar;
    }

    public void setCatchMusicBar(boolean isCatchMusicBar) {
        this.isCatchMusicBar = isCatchMusicBar;
        notifyChange();
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }

    private void notifyChange() {
        if (statusChangeListener != null) {
            statusChangeListener.onStatusChanged();
        }
    }

    public interface OnStatusChangeListener {
        void onStatusChanged();
    }
}
