package me.tsihen.qtools.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import com.google.android.material.appbar.MaterialToolbar
import me.tsihen.qtools.widget.ViewBuilder
import me.tsihen.util.log
import me.tsihen.util.startActivity

class SettingActivity : AbsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log.d("setting activity")

        val builder = ViewBuilder.RecyclerViewBuilder<ConstraintLayout>(this, margin = 32)

//        builder.add(
//            ViewBuilder.itemSwitch(
//                this,
//                "测试",
//                "仅供调试",
//                config.getOr("__test_enable", false)
//            ) { _, o ->
//                config["__test_enable"] = o
//            })
        builder.add(
            ViewBuilder.itemTitle(this, "脚本执行", "执行一些有趣的脚本", null)
        )
        builder.add(ViewBuilder.itemButton(this, "普通 Java 脚本", "QQ复读机样式的脚本") {
            this.startActivity<ScriptActivity>()
        })
        builder.add(ViewBuilder.itemButton(this, "标准 Java 类文件", "咕咕咕") {
            Toast.makeText(this, "咕咕咕", Toast.LENGTH_SHORT).show()
        })
        builder.add(
            ViewBuilder.itemTitle(this, "其他", "一些小把戏", null)
        )
        builder.add(ViewBuilder.itemButton(this, "语音误报时长", "咕咕咕") {
            Toast.makeText(this, "咕咕咕", Toast.LENGTH_SHORT).show()
        })
        builder.add(ViewBuilder.itemButton(this, "日志文件", log.file.path) {
            try {
                val intent = Intent()
                val file = log.file
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Intent.ACTION_VIEW
                val type = "text"
                intent.setDataAndType(Uri.fromFile(file), type)
                startActivity(intent)
            } catch (e: Throwable){
                Toast.makeText(this, "错误", Toast.LENGTH_SHORT).show()
            }
        })

        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        ll.addView(MaterialToolbar(this).apply {
            title = HtmlCompat.fromHtml("<strong>QQ工具</strong>", HtmlCompat.FROM_HTML_MODE_COMPACT)
        })
        ll.addView(builder.build().apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        })

        setContentView(ll)
    }
}