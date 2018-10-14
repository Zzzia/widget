package com.zia.widget.page

import android.Manifest
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.crashreport.CrashReport
import com.zia.widget.R
import com.zia.widget.bean.Config
import com.zia.widget.net.ApiGenerator
import com.zia.widget.net.ApiService
import com.zia.widget.util.*
import com.zia.widget.util.downlaodUtil.DownloadRunnable
import com.zia.widget.widget.little.LittleTransWidget
import com.zia.widget.widget.little.LittleWidget
import com.zia.widget.widget.normal.NormalWidget
import kotlinx.android.synthetic.main.widget_activity_main.*
import java.io.File
import java.io.IOException


/**
 * Created by zia on 2018/10/10.
 * 小部件独立模块的界面，用于登录，验证
 */
class MainActivity : AppCompatActivity() {

    private val redrock: ApiService by lazy { ApiGenerator.getRedrockService(ApiService::class.java) }
    private val zia: ApiService by lazy { ApiGenerator.getZiaService(ApiService::class.java) }

    private lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_activity_main)

        CrashReport.initCrashReport(applicationContext)

        rxPermissions = RxPermissions(this@MainActivity)

        widget_main_version.text = versionName

        widget_main_configBt.setOnClickListener { _ ->
            startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
        }

        widget_main_joinGroup.setOnClickListener {
            if (!joinQQGroup("DXvamN9Ox1Kthaab1N_0w7s5N3aUYVIf")) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val data = ClipData.newPlainText("QQ Group", "570919844")
                clipboard.primaryClip = data
                Toast.makeText(this, "抱歉，由于您未安装手机QQ或版本不支持，无法跳转至掌邮bug反馈群。" + "已将群号复制至您的手机剪贴板，请您手动添加",
                        Toast.LENGTH_LONG).show()
            }
        }

        Thread(Runnable {
            //版本升级控制
            try {
                val config = zia.getVersionConfig().execute().body()!!
                runOnUiThread {
                    if (config.version > version) {
                        showUpdateDialog(config)
                    }
                    if (config.able != "true") {
                        //服务器认证失败，无法访问
                        showErrorDialog("由于某些原因，软件不再提供使用")
                    } else {
                        initViews()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    showErrorDialog("网络错误，无法校验服务器，请稍后再试")
                }
            }
        }).start()
    }

    private fun initViews() {
        widget_main_loginBt.setOnClickListener { _ ->
            val stuNum = widget_main_stuNumEt.text.toString()

            if (stuNum.isEmpty()) {
                Toast.makeText(this, "请输入学号", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Thread(Runnable {
                try {
                    val data = redrock.getCourse(stuNum).execute().body()

                    if (data == null || data.isEmpty()) {
                        runOnUiThread {
                            Toast.makeText(this, "更新失败，请稍后尝试", Toast.LENGTH_SHORT).show()
                        }
                        return@Runnable
                    }

                    defaultSharedPreferences.editor {
                        putString(WIDGET_COURSE, data)
                        putBoolean(SP_WIDGET_NEED_FRESH, true)
                    }

                    runOnUiThread {
                        LittleTransWidget().refresh(this@MainActivity)
                        LittleWidget().refresh(this@MainActivity)
                        NormalWidget().fresh(this@MainActivity, 0)
                        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show()
                    }
                }
            }).start()
        }
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
                .setPositiveButton("确定") { _, _ -> android.os.Process.killProcess(android.os.Process.myPid()) }
                .setCancelable(false)
                .show()
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
}
