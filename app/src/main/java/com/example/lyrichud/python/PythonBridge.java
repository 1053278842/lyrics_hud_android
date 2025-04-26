package com.example.lyrichud.python;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.lyrichud.MainActivity;

public class PythonBridge implements OnLyricsSyncer {

    private static PythonBridge instance;
    private final PyObject pyMain;
    private final PyObject manager;

    private PythonBridge() {
        Python python = Python.getInstance();
        pyMain = python.getModule("main");
        // 创建 LyricsSyncManager 实例
        manager = pyMain.callAttr("KugouLyricManager");
    }

    public static synchronized PythonBridge getInstance() {
        if (instance == null) {
            instance = new PythonBridge();
        }
        return instance;
    }


    @Override
    public void sendSongInfo(String id, long position, String image, boolean status, String title, String artist, long duration) {
        try {
            manager.callAttr("send_kugou_lyrics", MainActivity.raspberry_ip, id, position, image, status, title, artist, duration);
        } catch (PyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPlayState(boolean status, long position) {
        try {
            manager.callAttr("sync_play_state", MainActivity.raspberry_ip, status, position);
        } catch (PyException e) {
            e.printStackTrace();
        }
    }
}
