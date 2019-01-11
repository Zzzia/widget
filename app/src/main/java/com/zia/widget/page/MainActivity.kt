package com.zia.widget.page

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.crashreport.CrashReport
import com.zia.widget.R
import com.zia.widget.bean.Config
import com.zia.widget.bean.Course
import com.zia.widget.net.Service
import com.zia.widget.util.*
import com.zia.widget.util.downlaodUtil.DownloadRunnable
import com.zia.widget.widget.little.LittleTransWidget
import com.zia.widget.widget.little.LittleWidget
import com.zia.widget.widget.normal.NormalWidget
import kotlinx.android.synthetic.main.widget_activity_main.*
import okhttp3.*
import java.io.File
import java.io.IOException


/**
 * Created by zia on 2018/10/10.
 * 小部件独立模块的界面，用于登录，验证
 */
class MainActivity : AppCompatActivity() {

    private lateinit var rxPermissions: RxPermissions

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_activity_main)

        CrashReport.initCrashReport(applicationContext)

        addShortcut()

        rxPermissions = RxPermissions(this@MainActivity)

        widget_main_version.text = "v${Version.packageName(this)}"

        checkVersion()

        widget_main_configBt.setOnClickListener { startActivity(Intent(this@MainActivity, ConfigActivity::class.java)) }

        widget_main_joinGroup.setOnClickListener { joinQQ() }

        initViews()

        val needFresh = intent.getBooleanExtra("needFresh", false)
        if (needFresh) {
            Toast.makeText(this, "正在更新", Toast.LENGTH_SHORT).show()
            refresh(defaultSharedPreferences.getString(SP_STUNUM, ""))
        }

        //修复自动更新bug，设置为不自动更新
        defaultSharedPreferences.editor {
            putBoolean(SP_IS_AUTO_UPDATE, false)
        }
    }

    private fun checkVersion() {
        val versionRequest = Request.Builder()
                .url("http://zzzia.net:8080/version/get")
                .post(FormBody.Builder().add("key", "widget").build())
                .build()

        showWaitDialog()
        Service.okHttpClient.newCall(versionRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    hideWaitDialog()
                    e.printStackTrace()
                    showErrorDialog("连接服务器失败，请检查网络")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()

                runOnUiThread {
                    hideWaitDialog()
                    val config = Gson().fromJson<Config>(json, Config::class.java)
                    if (config == null || config.able != "true") {
                        //服务器认证失败，无法访问
                        showErrorDialog("由于某些原因，软件不再提供使用")
                    } else if (config.version > Version.packageCode(this@MainActivity)) {
                        showUpdateDialog(config)
                    }
                }
            }

        })
    }

    private fun initViews() {
        //填充学号记录
        val stuNumHistory = defaultSharedPreferences.getString(SP_STUNUM, "")
        widget_main_stuNumEt.setText(stuNumHistory)
        //填充自动更新记录
//        val isAutoUpdate = defaultSharedPreferences.getBoolean(SP_IS_AUTO_UPDATE, false)
//        widget_main_autoUpdate_check.isChecked = isAutoUpdate
//
//        widget_main_autoUpdate_check.setOnTouchListener { _, _ ->
//            return@setOnTouchListener false
//        }

        //自动更新点击事件
//        widget_main_autoUpdate_layout.setOnClickListener {
//            val checked = !widget_main_autoUpdate_check.isChecked
//            widget_main_autoUpdate_check.isChecked = checked
//            Log.d(javaClass.simpleName, "checked:$checked")
//            defaultSharedPreferences.editor {
//                putBoolean(SP_IS_AUTO_UPDATE, checked)
//            }
//            if (checked) {
//                autoFreshCourse(this)
//            }
//        }

        //刷新课表点击事件
        widget_main_loginBt.setOnClickListener {

            val stuNum = widget_main_stuNumEt.text.toString()

            if (stuNum.isEmpty() || stuNum.length < 4) {
                Toast.makeText(this, "请输入学号", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            refresh(stuNum)
        }
    }

    private fun refresh(stuNum: String?) {
        if (stuNum == null || stuNum.isEmpty()) {
            Toast.makeText(this, "还没有填写账号", Toast.LENGTH_SHORT).show()
            return
        }
        val courseRequest = Request.Builder()
                .url("https://wx.idsbllp.cn/api/kebiao")
                .post(FormBody.Builder().add("stuNum", stuNum).build())
                .build()

        showWaitDialog()

        Service.okHttpClient.newCall(courseRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    hideWaitDialog()
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "网络出错", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                val course: Course
                try {
                    //服务器数据有问题时json解析会出错
                    course = Gson().fromJson<Course>(json, Course::class.java)
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this@MainActivity, "服务器数据出了点问题..", Toast.LENGTH_SHORT).show()
                    return
                }

                runOnUiThread {
                    hideWaitDialog()
                    if (course.data == null) {
                        Toast.makeText(this@MainActivity, "服务器没有课表...", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }
                    defaultSharedPreferences.editor {
                        putString(WIDGET_COURSE, json)
                        putBoolean(SP_WIDGET_NEED_FRESH, true)
                        putString(SP_STUNUM, stuNum)
                    }
                    LittleTransWidget().refresh(this@MainActivity)
                    LittleWidget().refresh(this@MainActivity)
                    NormalWidget().fresh(this@MainActivity, 0)
                    Toast.makeText(this@MainActivity, "更新成功", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun showUpdateDialog(config: Config) {
        AlertDialog.Builder(this)
                .setTitle("是否更新版本")
                .setMessage(config.message)
                .setNegativeButton("取消", null)
                .setPositiveButton("更新") { _, _ ->
                    rxPermissions
                            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe {
                                if (it) {
                                    downloadApk(config.url)
                                } else {
                                    Toast.makeText(this@MainActivity, "请提供文件读写权限..", Toast.LENGTH_SHORT).show()
                                }
                            }
                }
                .setCancelable(true)
                .show()
    }

    private fun downloadApk(url: String) {
        val dialog = ProgressDialog(this@MainActivity)
        dialog.setCancelable(false)
        dialog.progress = 0
        dialog.setTitle("正在下载")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.show()

        val apkName = "widget.apk"
        val savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

        val downloadRunnable = DownloadRunnable(url, savePath, apkName) { ratio, part, total ->
            runOnUiThread {
                if (ratio == 100F) {
                    dialog.dismiss()
                    val intent = Intent(Intent.ACTION_VIEW)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    } else {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    intent.setDataAndType(FileUtil.getFileUri(this@MainActivity, File(savePath, apkName)),
                            "application/vnd.android.package-archive")
                    this@MainActivity.startActivity(intent)
                    return@runOnUiThread
                }
                dialog.progress = ratio.toInt()
                dialog.setProgressNumberFormat(String.format("%.2fm / %.2fm", part / 1024f / 1024f, total / 1024f / 1024f))
            }
        }

        Thread(downloadRunnable).start()
    }

    private fun showErrorDialog(massage: String) {
        AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(massage)
                .setPositiveButton("确定") { _, _ -> finish() }
                .setCancelable(false)
                .show()
    }

    private val waitDialog by lazy {
        AlertDialog.Builder(this@MainActivity)
                .setTitle("请稍等")
                .setMessage("正在连接服务器")
                .setCancelable(false)
                .create()
    }

    private fun showWaitDialog() {
        if (!waitDialog.isShowing) {
            waitDialog.show()
        }
    }

    private fun hideWaitDialog() {
        if (waitDialog.isShowing) {
            waitDialog.dismiss()
        }
    }

    private fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.data = Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            startActivity(intent)
            true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
    }

    private fun joinQQ() {
        if (!joinQQGroup("DXvamN9Ox1Kthaab1N_0w7s5N3aUYVIf")) {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("QQ Group", "570919844")
            clipboard.primaryClip = data
            Toast.makeText(this, "抱歉，由于您未安装手机QQ或版本不支持，无法跳转至掌邮bug反馈群。" + "已将群号复制至您的手机剪贴板，请您手动添加",
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun addShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val intent = Intent(this, MainActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.putExtra("needFresh", true)
            val shortcut = ShortcutInfo.Builder(this, "update")
                    .setShortLabel("刷新课表")
                    .setLongLabel("刷新课表")
                    .setRank(1)
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher_round))
                    .setIntent(intent)
                    .build()
            ShortcutsUtil.addShortcut(this, shortcut)
        }
    }
}
