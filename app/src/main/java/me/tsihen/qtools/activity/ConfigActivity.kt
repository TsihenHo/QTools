package me.tsihen.qtools.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import me.tsihen.config.ConfigManager
import me.tsihen.qtools.BuildConfig
import me.tsihen.qtools.R
import me.tsihen.qtools.widget.ViewBuilder
import me.tsihen.util.ToastUtils
import me.tsihen.util.initForStubActivity
import me.tsihen.util.loader
import me.tsihen.util.log

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val ll = findViewById<LinearLayout>(R.id.ll_debug)

        if (BuildConfig.DEBUG) {
            val builder = ViewBuilder.RecyclerViewBuilder(this, margin = 16)

            builder.add(ViewBuilder.itemTitle(this, "标题", "子标题", null))
            builder.add(ViewBuilder.itemButton(this, "跳转测试", "debug") {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://github.com/GoldenHuaji/QScript")
                    startActivity(intent)
                    ToastUtils.success(this, "成功")
                } catch (e: Throwable) {
                    log.e(e)
                    ToastUtils.error(this, "失败")
                }
            })
            builder.add(ViewBuilder.itemButton(this, "类加载器", "获取类加载器") {
                AlertDialog.Builder(this)
                    .setMessage(ConfigManager::class.java.classLoader?.toString() ?: "null")
                    .show()
            })

            ll.addView(builder.build())
        }
    }
}