package com.example.lyrichud.python;

public interface OnLyricsSyncer {
    void sendSongInfo(String id, long position, String image, boolean status, String title, String artist, long duration);

    void sendPlayState(boolean status, long position);
}
