package com.example.lyrichud.inter;

public interface LyricsSyncer {
    void sendSongInfo(String id, long position, String image, boolean status, String title, String artist, long duration);

    void sendPlayState(boolean status, long position);
}
