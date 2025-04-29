package com.example.lyrichud.service.inter;

/**
 * 状态接口：热点启动/关闭回调,和Wifi Hotspot Manager类强相关
 */
public interface OnHotspotStateChangeListener {
    void onHotspotEnabled(String ssid, String password);

    void onHotspotDisabled(String ssid);
}
