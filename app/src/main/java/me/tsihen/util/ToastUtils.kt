package me.tsihen.util

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import me.tsihen.qtools.R

object ToastUtils {
    private var toast: Toast? = null

    @JvmOverloads
    @JvmStatic
    fun show(ctx: Context, text: CharSequence, length: Int = Toast.LENGTH_SHORT) {
        toast?.cancel()
        toast = Toast.makeText(ctx, text, length)
        toast!!.show()
    }

    @JvmOverloads
    @JvmStatic
    fun error(ctx: Context, text: CharSequence, length: Int = Toast.LENGTH_SHORT) {
        toast?.cancel()
        toast = Toast(ctx)
        toast?.let {
            it.view = buildView(ctx, false, text)
//            it.view = View.inflate(ctx, R.layout.layout_toast_fail, null).apply {
//                findViewById<TextView>(R.id.text).text = text
//            }
            it.duration = length
            it.show()
        }
    }

    @JvmOverloads
    @JvmStatic
    fun success(ctx: Context, text: CharSequence, length: Int = Toast.LENGTH_SHORT) {
        toast?.cancel()
        toast = Toast(ctx)
        toast?.let {
            it.view = buildView(ctx, true, text)
//            it.view = View.inflate(ctx, R.layout.layout_toast_success, null).apply {
//                findViewById<TextView>(R.id.text).text = text
//            }
            it.duration = length
            it.show()
        }
    }

    private fun buildView(ctx: Context, isSuccess: Boolean, text: CharSequence): View {
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.HORIZONTAL
        root.background = ContextCompat.getDrawable(ctx, R.drawable.bg_black_round)
        val imageView = ImageView(ctx)
        imageView.setImageDrawable(
            ContextCompat.getDrawable(
                ctx,
                if (isSuccess) R.drawable.ic_success_white else R.drawable.ic_failure_white
            )
        )
        imageView.imageTintList = ContextCompat.getColorStateList(
            ctx,
            if (isSuccess) R.color.shapeColor_6 else R.color.unusableColor
        )
        imageView.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, 80).apply {
            gravity = Gravity.CENTER_VERTICAL
            setMargins(0, 0, 5, 0)
        }
        root.addView(imageView)
        val textView = TextView(ctx)
        textView.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            .apply { gravity = Gravity.CENTER_VERTICAL }
        textView.setTextColor(ContextCompat.getColor(ctx, R.color.whiteTextColor))
        textView.text = text
        textView.textSize = 16f
        root.addView(textView)
        root.setPadding(32, 16, 32, 16)
        return root
    }
}