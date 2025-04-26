package com.example.lyrichud.service.inter;

/**
 * 关于音乐状态Bar的相关状态。强关联LyricForegroundService类
 */
public interface OnLyricBarChangeListener {
    void onKugouAppConnected();

    void onKugouAppDisconnected();

    void onServiceStateListener(boolean state);
}
