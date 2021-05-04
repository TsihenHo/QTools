package me.tsihen.qtools.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.google.android.material.appbar.MaterialToolbar
import me.tsihen.qtools.script.ScriptManager
import me.tsihen.qtools.widget.CustomDialog
import me.tsihen.qtools.widget.ViewBuilder
import me.tsihen.util.ToastUtils
import me.tsihen.util.log
import java.io.File

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
        val builder = ViewBuilder.RecyclerViewBuilder(this, 32)
        builder.add(ViewBuilder.itemButton(this, "立即应用更改/识别新脚本", "免重启，应用更改。可能有BUG") {
            try {
                ScriptManager.doInit()
                ToastUtils.success(this, "成功")
            } catch (e: Throwable) {
                log.e(e)
                ToastUtils.success(this, "错误")
            }
        })
        builder.add(ViewBuilder.itemButton(this, "打开脚本文件夹路径", "将脚本放在这里，点击上方按钮后可以识别") {
            try {
                val vmBuilder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(vmBuilder.build())
                vmBuilder.detectFileUriExposure()

                val intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.data = Uri.parse("file://" + ScriptManager.scriptDirPath + File.pathSeparator)
                if (intent.resolveActivityInfo(packageManager, 0) != null) {
                    startActivity(intent)
                } else {
                    ToastUtils.error(this, "找不到文件管理器", Toast.LENGTH_LONG)
                    val dialog = CustomDialog(this)
                    dialog.isSingle = true
                    dialog.positive = "好"
                    dialog.message = ScriptManager.scriptDirPath
                    dialog.title = "脚本文件夹路径"
                    dialog.onClickBottomListener = object : CustomDialog.OnClickBottomListener {
                        override fun onPositiveClick(it: View, dialog: CustomDialog) {
                            dialog.dismiss()
                        }

                        override fun onNegativeClick(it: View, dialog: CustomDialog) {
                        }
                    }
                    dialog.show()
                }
            } catch (e: Throwable) {
                log.e(e)
                ToastUtils.error(this, "出现错误")
            }
        })
        ScriptManager.scripts.forEach {
            if (it.file.exists()) builder.add(ViewBuilder.itemScript(this, it))
        }
        contentView.addView(builder.build())
    }
}