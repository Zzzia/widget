package com.zia.widget.widget.little

import android.content.Context
import android.widget.RemoteViews
import com.zia.widget.R
import com.zia.widget.bean.Course
import com.zia.widget.util.filterClassRoom
import com.zia.widget.util.getClickPendingIntent
import com.zia.widget.util.getWeekDayChineseName
import com.zia.widget.util.saveHashLesson
import java.util.*

/**
 * Created by zia on 2018/10/10.
 * 小型部件，不透明
 */
class LittleWidget : BaseLittleWidget() {
    override fun getLayoutResId(): Int {
        return R.layout.widget_little
    }

    override fun getShareName(): String {
        return "widget_share_little"
    }

    override fun getUpResId(): Int {
        return R.id.widget_little_up
    }

    override fun getDownResId(): Int {
        return R.id.widget_little_down
    }

    override fun getTitleResId(): Int {
        return R.id.widget_little_title
    }

    override fun getCourseNameResId(): Int {
        return R.id.widget_little_courseName
    }

    override fun getRoomResId(): Int {
        return R.id.widget_little_room
    }

    override fun getRefreshResId(): Int {
        return R.id.widget_little_refresh
    }

    override fun getRemoteViews(context: Context, course: Course.DataBean?, timeTv: String): RemoteViews {
        val rv = RemoteViews(context.packageName, getLayoutResId())
        rv.setOnClickPendingIntent(getUpResId(), getClickPendingIntent(context, getUpResId(), "btn.text.com", javaClass))
        rv.setOnClickPendingIntent(getDownResId(), getClickPendingIntent(context, getDownResId(), "btn.text.com", javaClass))
        rv.setOnClickPendingIntent(getRefreshResId(), getClickPendingIntent(context, getRefreshResId(), "btn.text.com", javaClass))
        if (course == null) {
            rv.setTextViewText(getTitleResId(), getWeekDayChineseName(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)))
            rv.setTextViewText(getCourseNameResId(), "今天没有课~")
            rv.setTextViewText(getRoomResId(), "")
        } else {
            //保存当前hash_lesson
            saveHashLesson(context, course.hash_lesson, getShareName())
            rv.setTextViewText(getTitleResId(), timeTv)
            rv.setTextViewText(getCourseNameResId(), course.course)
            rv.setTextViewText(getRoomResId(), filterClassRoom(course.classroom!!))
        }
        return rv
    }
}