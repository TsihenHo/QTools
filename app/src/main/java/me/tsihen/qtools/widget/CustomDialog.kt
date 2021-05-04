package me.tsihen.qtools.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import me.tsihen.qtools.R

class CustomDialog(context: Context?) :
    Dialog(context!!, R.style.CustomDialog) {
    /**
     * 显示的图片
     */
    private lateinit var imageIv: ImageView

    /**
     * 显示的标题
     */
    private lateinit var titleTv: TextView

    /**
     * 显示的消息
     */
    private lateinit var messageTv: TextView

    /**
     * 确认和取消按钮
     */
    private lateinit var negativeBn: Button
    private lateinit var positiveBn: Button

    /**
     * 按钮之间的分割线
     */
    private lateinit var columnLineView: View

    /**
     * 都是内容数据
     */
    var message = ""
    var title = ""
    var positive = ""
    var negative = ""
    var imageResId = -1

    /**
     * 底部是否只有一个按钮
     */
    var isSingle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dialog)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false)
        //初始化界面控件
        initView()
        //初始化界面数据
        refreshView()
        //初始化界面控件的事件
        initEvent()
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private fun initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        positiveBn.setOnClickListener {
            onClickBottomListener?.onPositiveClick(it, this)
        }
        //设置取消按钮被点击后，向外界提供监听
        negativeBn.setOnClickListener {
            onClickBottomListener?.onNegativeClick(it, this)
        }
    }

    /**
     * 初始化界面控件的显示数据
     */
    private fun refreshView() {
        //如果用户自定了title和message
        if (!TextUtils.isEmpty(title)) {
            titleTv.text = title
            titleTv.visibility = View.VISIBLE
        } else {
            titleTv.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(message)) {
            messageTv.text = message
        }
        //如果设置按钮的文字
        if (!TextUtils.isEmpty(positive)) {
            positiveBn.text = positive
        } else {
            positiveBn.text = "确定"
        }
        if (!TextUtils.isEmpty(negative)) {
            negativeBn.text = negative
        } else {
            negativeBn.text = "取消"
        }
        if (imageResId != -1) {
            imageIv.setImageResource(imageResId)
            imageIv.visibility = View.VISIBLE
        } else {
            imageIv.visibility = View.GONE
        }
        /**
         * 只显示一个按钮的时候隐藏取消按钮，回掉只执行确定的事件
         */
        if (isSingle) {
            columnLineView.visibility = View.GONE
            negativeBn.visibility = View.GONE
        } else {
            negativeBn.visibility = View.VISIBLE
            columnLineView.visibility = View.VISIBLE
        }
    }

    override fun show() {
        super.show()
        refreshView()
    }

    /**
     * 初始化界面控件
     */
    private fun initView() {
        negativeBn = findViewById<View>(R.id.negative) as Button
        positiveBn = findViewById<View>(R.id.positive) as Button
        titleTv = findViewById<View>(R.id.title) as TextView
        messageTv = findViewById<View>(R.id.message) as TextView
        imageIv = findViewById<View>(R.id.image) as ImageView
        columnLineView = findViewById(R.id.column_line)
    }

    /**
     * 设置确定取消按钮的回调
     */
    var onClickBottomListener: OnClickBottomListener? = null

    interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        fun onPositiveClick(it: View, dialog: CustomDialog)

        /**
         * 点击取消按钮事件
         */
        fun onNegativeClick(it: View, dialog: CustomDialog)
    }
}