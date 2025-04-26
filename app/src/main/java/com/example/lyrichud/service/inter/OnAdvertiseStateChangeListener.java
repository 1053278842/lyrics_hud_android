package com.example.lyrichud.service.inter;

/**
 * 状态接口：广播开始/停止回调,和Ble Advertiser类强相关
 */
public interface OnAdvertiseStateChangeListener {
    void onAdvertiseStarted();

    void onAdvertiseStopped();

    void onAdvertiseFailed(int errorCode);
}