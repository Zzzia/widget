package com.zia.widget.util;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

/**
 * Created by zia on 2018/12/14.
 */
public class ShortcutsUtil {

    /**
     * 删除shortcut
     *
     * @param context
     * @param id
     */
    public static void removeShortcut(Context context, String id) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            try {
                ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
                shortcutManager.removeDynamicShortcuts(Collections.singletonList(id));
            } catch (Exception e) {
                e.printStackTrace();
                if (context != null) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static void setShortcuts(Context context, List<ShortcutInfo> infos) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            try {
                ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
                shortcutManager.setDynamicShortcuts(infos);
            } catch (Exception e) {
                e.printStackTrace();
                if (context != null) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 添加shortcut
     *
     * @param context
     * @param shortcutInfo
     */
    public static void addShortcut(Context context, ShortcutInfo shortcutInfo) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
                shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcutInfo));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (context != null) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 添加shortcut到指定position
     *
     * @param context
     * @param shortcutInfo
     * @param index
     */
    public static void addShortcut(Context context, ShortcutInfo shortcutInfo, int index) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
                List<ShortcutInfo> dynamicShortcuts = shortcutManager.getDynamicShortcuts();
                for (ShortcutInfo dynamicShortcut : dynamicShortcuts) {
                    if (dynamicShortcut.getId().equals(shortcutInfo.getId())) {
                        return;
                    }
                }
                dynamicShortcuts.add(index, shortcutInfo);
                setShortcuts(context, dynamicShortcuts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (context != null) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

}
