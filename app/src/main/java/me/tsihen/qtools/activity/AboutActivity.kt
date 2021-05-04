package me.tsihen.qtools.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.appbar.MaterialToolbar
import me.tsihen.qtools.R
import me.tsihen.qtools.widget.ViewBuilder
import me.tsihen.util.ToastUtils
import me.tsihen.util.log

class AboutActivity : AbsActivity() {
    companion object {
        const val R_ID_MAIN = 0x3d000001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val root = findViewById<RelativeLayout>(R.id.root)

        val builder = ViewBuilder.RecyclerViewBuilder(this, margin = 32)
        builder.add(ViewBuilder.itemButton(this, "作者", "子恒", null))
        builder.add(ViewBuilder.itemButton(this, "Telegram频道", "仅用于发布更新") {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://t.me/QScript")
            startActivity(intent)
        })
        builder.add(ViewBuilder.itemButton(this, "我的QQ", "没有什么特别的") {
            try {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("MyQQNumber", "3318448676")
                clipboard.setPrimaryClip(clip)
                ToastUtils.success(this, "QQ号码已经复制到您的剪切板")
            } catch (e: Exception) {
                ToastUtils.error(this, "执行时遇到错误")
            }
        })
        builder.add(ViewBuilder.itemButton(this, "我的邮箱", "或许可以在这里反馈？") {
            try {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:") // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, "3318448676@qq.com")
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            } catch(e: Throwable) {
                log.e(e)
                ToastUtils.error(this, "错误")
            }
        })
        builder.add(ViewBuilder.itemButton(this, "开源地址", "Github") {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://github.com/GoldenHuaji/QScript")
                startActivity(intent)
            } catch (e: Throwable) {
                log.e(e)
                ToastUtils.error(this, "失败")
            }
        })

        val recyclerView = builder.build()
        recyclerView.id = R_ID_MAIN
        root.addView(builder.build().apply {
            layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).also {
                it.addRule(RelativeLayout.CENTER_IN_PARENT)
                it.addRule(RelativeLayout.BELOW, R.id.app_name)
            }
        })
        findViewById<LinearLayout>(R.id.root_view).addView(MaterialToolbar(this).apply {
            title = this@AboutActivity.getString(R.string.about)
        }, 0)
    }
}