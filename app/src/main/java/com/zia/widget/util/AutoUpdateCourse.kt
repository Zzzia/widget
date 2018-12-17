package com.zia.widget.util

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.zia.widget.bean.Course
import com.zia.widget.net.Service
import com.zia.widget.widget.little.LittleTransWidget
import com.zia.widget.widget.little.LittleWidget
import com.zia.widget.widget.normal.NormalWidget
import okhttp3.FormBody
import okhttp3.Request
import java.util.*

/**
 * Created by zia on 2018/12/17.
 */

/**
 * 自动更新课表
 */
//同步方法，避免不必要的网络请求
@Synchronized fun autoFreshCourse(context: Context) {
    //如果没开启该功能
    if (!context.defaultSharedPreferences.getBoolean(SP_IS_AUTO_UPDATE, false)){
        return
    }
    //获取应该更新的时间
    val updateTime = context.defaultSharedPreferences
            .getLong(SP_COURSE_UPDATE_TIME, Calendar.getInstance().timeInMillis - 1)
    //如果当前时间大于应该更新的时间，并且是晚上12点，那么更新
    val calendar = Calendar.getInstance()
    if (calendar.timeInMillis > updateTime && calendar.get(Calendar.HOUR_OF_DAY) == 0) {
        context.startService(Intent(context, UpdateCourseService::class.java))
    }
}

class UpdateCourseService : IntentService("UpdateCourseService") {
    override fun onHandleIntent(intent: Intent?) {
        val stuNum = defaultSharedPreferences.getString(SP_STUNUM, "")
        if (stuNum == null || stuNum.isEmpty()) return

        val courseRequest = Request.Builder()
                .url("https://wx.idsbllp.cn/api/kebiao")
                .post(FormBody.Builder().add("stuNum", stuNum).build())
                .build()
        try {
            val json = Service.okHttpClient.newCall(courseRequest).execute().body()?.string()
            val course: Course = Gson().fromJson<Course>(json, Course::class.java)
            if (course.data == null) {
                return
            }
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, 7)
            defaultSharedPreferences.editor {
                putString(WIDGET_COURSE, json)
                putBoolean(SP_WIDGET_NEED_FRESH, true)
                putString(SP_STUNUM, stuNum)
                putLong(SP_COURSE_UPDATE_TIME, calendar.timeInMillis)
            }
            LittleTransWidget().refresh(this@UpdateCourseService)
            LittleWidget().refresh(this@UpdateCourseService)
            NormalWidget().fresh(this@UpdateCourseService, 0)
            Log.d(javaClass.simpleName, "autoUpdate:\n$json")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}