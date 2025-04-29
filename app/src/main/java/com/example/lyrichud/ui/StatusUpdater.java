package com.example.lyrichud.ui;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lyrichud.R;
import com.example.lyrichud.model.StatusModel;

public class StatusUpdater {

    private final Activity activity;

    public StatusUpdater(Activity activity) {
        this.activity = activity;
    }

    // 更新状态的方法
    public void update() {
        StatusModel model = StatusModel.getInstance();

        // 获取 status_notice LinearLayout
        LinearLayout statusNotice = activity.findViewById(R.id.status_notice);

        // 获取每一个 include 项的引用
        LinearLayout noticeItem = (LinearLayout) statusNotice.getChildAt(0);
        LinearLayout musicBarItem = (LinearLayout) statusNotice.getChildAt(1);
        LinearLayout hotspotItem = (LinearLayout) statusNotice.getChildAt(2);
        LinearLayout bleItem = (LinearLayout) statusNotice.getChildAt(3);
        LinearLayout peripheralItem = (LinearLayout) statusNotice.getChildAt(4);

        // 动态设置每个项的文本和图标
        updateItem(noticeItem, R.string.notice, model.isNotificationListening());
        updateItem(musicBarItem, R.string.musicBar, model.isCatchMusicBar());
        updateItem(hotspotItem, R.string.hotspot, model.isHotspotOn());
        updateItem(bleItem, R.string.ble, model.isAdvertising());
        updateItem(peripheralItem, R.string.peripheral, model.isPeripheralConnected());
    }

    // 辅助方法来更新每个项的文本和图标
    private void updateItem(LinearLayout item, int textResId, boolean status) {
        ImageView icon = item.findViewById(R.id.icon);
        TextView text = item.findViewById(R.id.text);

        // 根据状态更新图标和文本
        icon.setImageResource(status ? R.drawable.ic_verified : R.drawable.ic_error);
        text.setText(textResId);
        icon.setColorFilter(status ? activity.getColor(R.color.colorAccent) : activity.getColor(R.color.colorFail));
    }
}
