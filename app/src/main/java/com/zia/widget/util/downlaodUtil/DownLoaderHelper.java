package com.zia.widget.util.downlaodUtil;

import android.util.Log;

import java.io.File;

public class DownLoaderHelper {
    public static boolean mkDir(String root) {
        File file = new File(root);
        if (!file.exists() && !file.mkdirs()) {
            Log.d("DownLoaderHelper", "路径创建失败..");
            return false;
        }
        return true;
    }

    public static String convertSize(int fileSize) {
        float kb = fileSize / 1024;
        float mb = kb / 1024;
        float gb = mb / 1024;
        if (gb != 0) {
            return mb / 1024f + " Gb";
        } else if (mb != 0) {
            return kb / 1024f + " Mb";
        } else {
            return fileSize / 1024f + " Kb";
        }
    }

    /**
     * 检查路径并返回带有/结尾的路径
     *
     * @param source 路径
     * @return
     */
    static String optimizeRoot(String source) {
        source = source.trim();
        int length = source.length();
        if (source.charAt(length - 1) != File.separatorChar) {
            source = source + File.separatorChar;
        }
        return source;
    }
}
