package me.tsihen.qtools.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import me.tsihen.qtools.R
import me.tsihen.qtools.widget.ViewBuilder

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val ll = findViewById<LinearLayout>(R.id.ll_debug)

        val builder = ViewBuilder.RecyclerViewBuilder<ConstraintLayout>(this, margin = 16)

        builder.add(ViewBuilder.itemSwitch(this, "测试", "仅供调试", false, null))
        builder.add(ViewBuilder.itemTitle(this, "标题", "子标题", null))
        builder.add(ViewBuilder.itemButton(this, "测试", "尽快尽快", null))

        ll.addView(builder.build())
    }
}