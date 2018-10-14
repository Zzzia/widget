package com.zia.widget.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import java.io.File

/**
 * Created by zia on 2018/10/13.
 */
object FileUtil {
    fun getFileUri(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(context, "com.zia.widget.FileProvider", file)
        } else {
            Uri.fromFile(file)
        }
    }
}
