package com.example.lyrichud;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.lyrichud.inter.LyricsSyncer;

public class PythonBridge implements LyricsSyncer {

    private static PythonBridge instance;
    private final PyObject pyMain;

    private PythonBridge() {
        Python python = Python.getInstance();
        pyMain = python.getModule("main");
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
            pyMain.callAttr("send_kugou_lyrics", id, position, image, status, title, artist, duration);
        } catch (PyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPlayState(boolean status, long position) {
        try {
            pyMain.callAttr("sync_play_state", status, position);
        } catch (PyException e) {
            e.printStackTrace();
        }
    }
}
