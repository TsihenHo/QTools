package me.tsihen.qtools.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import me.tsihen.qtools.R
import me.tsihen.qtools.activity.EachScriptSettingActivity
import me.tsihen.qtools.script.Script
import me.tsihen.qtools.script.ScriptManager
import me.tsihen.util.dip2sp
import me.tsihen.util.startActivity

object ViewBuilder {
    const val R_ID_TITLE = 0x3d00ff00
    const val R_ID_DESC = 0x3d00ff01
    const val R_ID_SWITCH = 0x3d00ff02
    const val R_ID_ARROW = 0x3d00ff03

    @JvmStatic
    fun itemSwitch(
        ctx: Context,
        title: CharSequence,
        desc: CharSequence,
        on: Boolean,
        listener: CompoundButton.OnCheckedChangeListener?
    ): ConstraintLayout {
        val root = ConstraintLayout(ctx)
        root.id = title.hashCode()
        root.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        val tvTitle = TextView(ctx).also { it.text = title }
        tvTitle.id = R_ID_TITLE
        tvTitle.textSize = 18f
        tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.secondTextColor))
        root.addView(tvTitle)

        val sw = SwitchMaterial(ctx).also { it.isChecked = on }
        sw.id = R_ID_SWITCH
        sw.setOnCheckedChangeListener(listener)
        sw.layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            val m = dip2sp(ctx, 14f)
            setMargins(m, m, 0, 0)
        }
        root.addView(sw)

        val tvDesc = TextView(ctx).also { it.text = desc }
        tvDesc.id = R_ID_DESC
        tvDesc.textSize = 14f
        tvDesc.setTextColor(ContextCompat.getColor(ctx, R.color.thirdTextColor))
        root.addView(tvDesc)

        ConstraintSet().apply {
            clone(root)

            connect(R_ID_TITLE, ConstraintSet.TOP, root.id, ConstraintSet.TOP, 32)
            connect(R_ID_TITLE, ConstraintSet.START, root.id, ConstraintSet.START, 32)

            connect(R_ID_DESC, ConstraintSet.TOP, R_ID_TITLE, ConstraintSet.BOTTOM, 16)
            connect(R_ID_DESC, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM, 32)
            connect(R_ID_DESC, ConstraintSet.START, root.id, ConstraintSet.START, 32)

            connect(R_ID_SWITCH, ConstraintSet.END, root.id, ConstraintSet.END)
            connect(R_ID_SWITCH, ConstraintSet.TOP, root.id, ConstraintSet.TOP)
            connect(R_ID_SWITCH, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM)

            applyTo(root)
        }

        return root
    }

    @JvmStatic
    fun itemButton(
        ctx: Context,
        title: CharSequence,
        desc: CharSequence,
        listener: View.OnClickListener?
    ): ConstraintLayout {
        val root = ConstraintLayout(ctx)
        root.id = title.hashCode()
        root.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        ViewCompat.setBackground(root, ContextCompat.getDrawable(ctx, R.drawable.bg_ripple))

        val tvTitle = TextView(ctx).also { it.text = title }
        tvTitle.id = R_ID_TITLE
        tvTitle.textSize = 18f
        tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.secondTextColor))
        root.addView(tvTitle)

        val tvDesc = TextView(ctx).also { it.text = desc }
        tvDesc.id = R_ID_DESC
        tvDesc.textSize = 14f
        tvDesc.setTextColor(ContextCompat.getColor(ctx, R.color.thirdTextColor))
        root.addView(tvDesc)

        val iv = ImageView(ctx)
        iv.id = R_ID_ARROW
        iv.layoutParams = ViewGroup.LayoutParams(66, 66)
        iv.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_arrow))
        root.addView(iv)

        ConstraintSet().apply {
            clone(root)

            connect(R_ID_TITLE, ConstraintSet.TOP, root.id, ConstraintSet.TOP, 32)
            connect(R_ID_TITLE, ConstraintSet.START, root.id, ConstraintSet.START, 32)

            connect(R_ID_DESC, ConstraintSet.TOP, R_ID_TITLE, ConstraintSet.BOTTOM, 16)
            connect(R_ID_DESC, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM, 32)
            connect(R_ID_DESC, ConstraintSet.START, root.id, ConstraintSet.START, 32)

            connect(R_ID_ARROW, ConstraintSet.END, root.id, ConstraintSet.END, 32)
            connect(R_ID_ARROW, ConstraintSet.TOP, root.id, ConstraintSet.TOP)
            connect(R_ID_ARROW, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM)

            applyTo(root)
        }

        root.setOnClickListener(listener)

        return root
    }

    fun itemTitle(
        ctx: Context,
        title: CharSequence,
        desc: CharSequence,
        listener: View.OnClickListener?
    ): ConstraintLayout {
        val root = ConstraintLayout(ctx)

        root.id = title.hashCode()
        root.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        val titleString =
            HtmlCompat.fromHtml("<strong>$title</strong>", HtmlCompat.FROM_HTML_MODE_COMPACT)
        val descString =
            HtmlCompat.fromHtml("<strong>$desc</strong>", HtmlCompat.FROM_HTML_MODE_COMPACT)

        val tvTitle = TextView(ctx).also { it.text = titleString }
        tvTitle.id = R_ID_TITLE
        tvTitle.textSize = 24f
        tvTitle.setTextColor(ContextCompat.getColor(ctx, R.color.firstTextColor))
        root.addView(tvTitle)

        val tvDesc = TextView(ctx).also { it.text = descString }
        tvDesc.id = R_ID_DESC
        tvDesc.textSize = 16f
        tvDesc.setTextColor(ContextCompat.getColor(ctx, R.color.secondTextColor))
        root.addView(tvDesc)

        ConstraintSet().apply {
            clone(root)

            connect(R_ID_TITLE, ConstraintSet.TOP, root.id, ConstraintSet.TOP, 32)
            connect(R_ID_TITLE, ConstraintSet.START, root.id, ConstraintSet.START, 32)

            connect(R_ID_DESC, ConstraintSet.TOP, R_ID_TITLE, ConstraintSet.BOTTOM, 16)
            connect(R_ID_DESC, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM, 32)
            connect(R_ID_DESC, ConstraintSet.START, root.id, ConstraintSet.START, 32)

            applyTo(root)
        }

        root.setOnClickListener(listener)

        return root
    }

    fun itemSwitchAndButton(
        ctx: Context,
        title: CharSequence,
        desc: CharSequence,
        on: Boolean,
        onCheckedChangeListener: CompoundButton.OnCheckedChangeListener,
        onClickListener: View.OnClickListener
    ): ConstraintLayout {
        val root =
            itemButton(ctx, title, desc, onClickListener)

        val sw = SwitchMaterial(ctx).also { it.isChecked = on }
        sw.id = R_ID_SWITCH
        sw.setOnCheckedChangeListener(onCheckedChangeListener)
        sw.layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            val m = dip2sp(ctx, 14f)
            setMargins(m, m, 0, 0)
        }
        root.addView(sw)

        ConstraintSet().apply {
            clone(root)

//            connect(R_ID_ARROW, ConstraintSet.END, root.id, ConstraintSet.END, 32)
//            connect(R_ID_ARROW, ConstraintSet.TOP, root.id, ConstraintSet.TOP)
//            connect(R_ID_ARROW, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM)

            connect(R_ID_SWITCH, ConstraintSet.END, R_ID_ARROW, ConstraintSet.START)
            connect(R_ID_SWITCH, ConstraintSet.TOP, root.id, ConstraintSet.TOP)
            connect(R_ID_SWITCH, ConstraintSet.BOTTOM, root.id, ConstraintSet.BOTTOM)

            applyTo(root)
        }

        return root
    }

    fun itemScript(ctx: Context, script: Script): ConstraintLayout {
        val root = itemSwitchAndButton(
            ctx,
            script.file.name,
            script.file.readLines()[0],
            script.enable,
            { _, _ -> ScriptManager.changeEnable(script) },
            { ctx.startActivity<EachScriptSettingActivity>("file_path" to script.file.absolutePath) })

        return root
    }

    class RecyclerViewBuilder<T : View> @JvmOverloads constructor(
        private val ctx: Context,
        private val margin: Int,
        @RecyclerView.Orientation private val orientation: Int = LinearLayoutManager.VERTICAL
    ) {
        private val result = RecyclerView(ctx)
        private val viewList = mutableListOf<T>()

        fun build(): RecyclerView = result.apply {
            val mgr = LinearLayoutManager(ctx)
            @SuppressLint("WrongConstant")
            mgr.orientation = orientation

            layoutManager = mgr
            adapter = object : RecyclerView.Adapter<MyViewHolder>() {
                override fun getItemCount(): Int = viewList.size
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                    return MyViewHolder(LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                    })
                }

                override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                    val v = viewList[position]
                    v.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(margin, margin, margin, 0)
                    }
                    holder.item.addView(v)
                }
            }
        }

        fun add(v: T): Boolean = viewList.add(v)
        fun add(v: T, index: Int): Unit = viewList.add(index, v)
        fun delete(index: Int): T = viewList.removeAt(index)

        class MyViewHolder(var item: LinearLayout) : RecyclerView.ViewHolder(item.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })
    }
}