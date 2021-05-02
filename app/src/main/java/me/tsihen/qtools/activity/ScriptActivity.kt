package me.tsihen.qtools.activity

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import com.google.android.material.appbar.MaterialToolbar
import me.tsihen.qtools.script.ScriptManager
import me.tsihen.qtools.widget.ViewBuilder
import me.tsihen.util.log

class ScriptActivity : AbsActivity() {
    private val contentView: ViewGroup by lazy {
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        ll.addView(MaterialToolbar(this).apply {
            title = HtmlCompat.fromHtml("<strong>脚本管理</strong>", HtmlCompat.FROM_HTML_MODE_COMPACT)
        })
        ll
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView)
    }

    override fun onResume() {
        super.onResume()
        while (contentView.childCount >= 2) {
            contentView.removeViewAt(1)
        }
        val builder = ViewBuilder.RecyclerViewBuilder<ConstraintLayout>(this, 32)
        builder.add(ViewBuilder.itemButton(this, "立即应用更改", "免重启，应用更改。可能有BUG") {
            try {
                ScriptManager.doInit()
                Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show()
            } catch (e: Throwable){
                log.e(e)
                Toast.makeText(this, "失败", Toast.LENGTH_SHORT).show()
            }
        })
        ScriptManager.scripts.forEach {
            builder.add(ViewBuilder.itemScript(this, it))
        }
        contentView.addView(builder.build())
    }
}