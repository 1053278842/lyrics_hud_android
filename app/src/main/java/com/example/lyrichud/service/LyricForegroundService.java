package com.example.lyrichud.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.lyrichud.MyNotificationListenerService;
import com.example.lyrichud.R;
import com.example.lyrichud.model.StatusModel;
import com.example.lyrichud.python.OnLyricsSyncer;
import com.example.lyrichud.python.PythonBridge;
import com.example.lyrichud.service.inter.OnLyricBarChangeListener;

import java.util.List;

public class LyricForegroundService extends Service {
    private static OnLyricBarChangeListener statusListener;
    private final String TAG = "LyricHud";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final IBinder binder = new LocalBinder();
    private MediaController kugouController;
    private boolean controllerInitialized = false;
    private OnLyricsSyncer pythonBridge;
    // 去重缓存变量
    private String lastTitle = null;
    private String lastArtist = null;
    private long lastPosition = -2;
    private int lastState = -1;


    @Override
    public void onCreate() {
        super.onCreate();
        // ✅ 确保 Python 环境已初始化
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // 发布常驻通知
        createAndStartForeground();

        // 实例化推送接口，pythonBridge是使用python实现的接口实现
        pythonBridge = PythonBridge.getInstance();

        handler.post(pollingRunnable);
    }

    private void createAndStartForeground() {
        String channelId = "lyric_service_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    channelId,
                    "歌词后台保活",
                    NotificationManager.IMPORTANCE_DEFAULT  // ≥ DEFAULT to show status‑bar icon
            );
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(chan);
        }
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("歌词助手")
                .setContentText("正在后台轮询播放状态…")
                .setSmallIcon(R.drawable.loge)
                .setOngoing(true)      // 持久通知 :contentReference[oaicite:9]{index=9}
                .build();
        startForeground(1, notification);
    }

    private void refreshMediaControllerIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaSessionManager manager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            ComponentName listenerComponent = new ComponentName(this, MyNotificationListenerService.class);
            try {
                List<MediaController> controllers = manager.getActiveSessions(listenerComponent);
                for (MediaController controller : controllers) {
                    if ("com.kugou.android".equals(controller.getPackageName())) {
                        if (kugouController == null || !kugouController.getSessionToken().equals(controller.getSessionToken())) {
                            if (kugouController != null) {
                                kugouController.unregisterCallback(kugouCallback);
                            }
                            kugouController = controller;
                            kugouController.registerCallback(kugouCallback);
                            controllerInitialized = true;

                            this.triggerCallbacks();
                            StatusModel.getInstance().setCatchMusicBar(true);
                            Log.d(TAG, "🔁 MediaSession 变更，重新绑定回调");
//                            triggerCallbacks();
                        }
                        return;  // 找到了，提前返回
                    }
                }
                // 如果列表里没有酷狗，说明它被关闭了，清空 controller
                if (kugouController != null) {
                    kugouController.unregisterCallback(kugouCallback);
                    kugouController = null;
                    controllerInitialized = false;
                    // 推送空格数据，中断外设
                    pythonBridge.sendSongInfo("", 0, null, false, "", "", 0);
                    StatusModel.getInstance().setCatchMusicBar(false);
                    Log.d(TAG, "🛑 酷狗已关闭，清空 MediaController");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "❌ 获取 MediaSession 权限失败：请确保已授予通知访问权限", e);
            }
        }
    }

    public void triggerCallbacks() {
        if (kugouController != null) {
            // 手动触发 onMetadataChanged
            MediaMetadata metadata = kugouController.getMetadata();
            if (metadata != null) {

                String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
                Log.d(TAG, "🎵 歌曲名: " + title);
                Log.d(TAG, "🎤 歌手: " + artist);
                Log.d(TAG, "🎤 歌曲时长: " + duration);


                // 手动触发 onPlaybackStateChanged
                PlaybackState state = kugouController.getPlaybackState();
                if (state != null) {
                    long pos = state.getPosition();
                    int s = state.getState();
                    Log.d(TAG, (s == PlaybackState.STATE_PLAYING ? "▶ 正在播放" : "⏸ 已暂停") + "，位置: " + pos);
                    pythonBridge.sendSongInfo(title + artist, pos, null, s == PlaybackState.STATE_PLAYING, title, artist, duration);
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        StatusModel.getInstance().setNotificationListening(true);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        stopForeground(true);
        if (kugouController != null && kugouCallback != null) {
            kugouController.unregisterCallback(kugouCallback);
        }

        // 确保通知被取消
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);  // 根据通知ID取消
        StatusModel.getInstance().setNotificationListening(false);
        Log.d(TAG, "进程终止!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }


    // 让外部可以获取到service对象
    public class LocalBinder extends Binder {
        public LyricForegroundService getService() {
            return LyricForegroundService.this;
        }
    }

    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMediaControllerIfNeeded();
            handler.postDelayed(this, 1000);  // 保留每秒检查是否已初始化
        }
    };


    private final MediaController.Callback kugouCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            if (metadata != null) {
                String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
                if (!TextUtils.equals(title, lastTitle) || !TextUtils.equals(artist, lastArtist)) {
                    lastTitle = title;
                    lastArtist = artist;
                    Log.d(TAG, "🎵 歌曲名: " + title);
                    Log.d(TAG, "🎤 歌手: " + artist);
                    Log.d(TAG, "💿 专辑: " + metadata.getString(MediaMetadata.METADATA_KEY_ALBUM));
                    Log.d(TAG, "💿 专辑作者: " + metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST));
                    Log.d(TAG, "歌曲时长（ms）: " + duration);
                    pythonBridge.sendSongInfo(title + artist, 0, null, true, title, artist, duration);

                    // 延迟 5 秒调用一次 onPlaybackStateChanged
                    handler.postDelayed(() -> {
                        if (kugouController != null && kugouController.getPlaybackState() != null) {
                            Log.d(TAG, "切歌后5s,自动同步一次进度*减少误差*");
                            kugouCallback.onPlaybackStateChanged(kugouController.getPlaybackState());
                        }
                    }, 5000);
                    // 延迟 5 秒调用一次 onPlaybackStateChanged
                    handler.postDelayed(() -> {
                        if (kugouController != null && kugouController.getPlaybackState() != null) {
                            Log.d(TAG, "切歌后10s,自动同步一次进度*减少误差*");
                            kugouCallback.onPlaybackStateChanged(kugouController.getPlaybackState());
                        }
                    }, 10000);
                }
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            if (state != null) {
                long pos = state.getPosition();
                int s = state.getState();
                if (s != lastState || Math.abs(pos - lastPosition) > 2000) {
                    lastState = s;
                    lastPosition = pos;
                    Log.d(TAG, (s == PlaybackState.STATE_PLAYING ? "▶ 正在播放" : "⏸ 已暂停") + "，位置: " + pos);
                    pythonBridge.sendPlayState(s == PlaybackState.STATE_PLAYING, pos);
                }
            }
        }
    };


}

