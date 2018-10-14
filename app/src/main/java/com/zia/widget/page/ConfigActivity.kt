package com.zia.widget.page

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.zia.widget.R
import com.zia.widget.page.little.LittleConfigActivity
import com.zia.widget.page.normal.NormalConfigActivity
import com.zia.widget.page.trans.TransConfigActivity
import kotlinx.android.synthetic.main.widget_activity_config.*


/**
 * Created by zia on 2018/10/11.
 * 设置主页面
 */
class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_activity_config)

        widget_config_littleTransLayout.setOnClickListener {
            startActivity(Intent(this@ConfigActivity, TransConfigActivity::class.java))
        }

        widget_config_littleLayout.setOnClickListener {
            startActivity(Intent(this@ConfigActivity, LittleConfigActivity::class.java))
        }

        widget_config_normalLayout.setOnClickListener {
            startActivity(Intent(this@ConfigActivity, NormalConfigActivity::class.java))
        }
    }
}
