package com.example.lyrichud.service.inter;

/**
 * 状态接口：外设连接/断开回调,和BleScanner Manager类强相关
 */
public interface OnPeripheralConnectListener {
    void onPeripheralConnected(String ipAddress);

    void onPeripheralDisconnected();
}