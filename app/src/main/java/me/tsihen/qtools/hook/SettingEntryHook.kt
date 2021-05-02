package me.tsihen.qtools.hook

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import de.robv.android.xposed.XposedHelpers
import me.tsihen.qtools.BuildConfig
import me.tsihen.qtools.activity.SettingActivity
import me.tsihen.util.*
import me.tsihen.xposed.AbsHook

object SettingEntryHook : AbsHook() {
    override fun doInit(): Boolean {
        try {
            getClass("$PACKAGE_QQ.activity.QQSettingSettingActivity").getDeclaredMethod(
                "doOnCreate",
                Bundle::class.java
            ).after {
                try {
                    val itemRef =
                        getObjectOrNull(
                            it.thisObject,
                            ".*",
                            getClassOrNull("$PACKAGE_QQ.widget.FormSimpleItem")
                        ) as View? ?: getObjectOrNull(
                            it.thisObject,
                            ".*",
                            getClassOrNull("$PACKAGE_QQ.widget.FormCommonSingleLineItem")
                                ?: getClass("$PACKAGE_QQ.widget.FormSimpleItem")
                        ) as View?
                    if (itemRef == null) {
                        log.e(RuntimeException("找不到设置入口"))
                        return@after
                    }
                    val item = XposedHelpers.findConstructorBestMatch(
                        itemRef.javaClass, Context::class.java
                    ).newInstance(it.thisObject as Context) as View
                    item.callVirtualMethod("setLeftText", "QQ工具")
                    item.callVirtualMethod("setBgType", 2)
                    item.callVirtualMethod("setRightText", BuildConfig.VERSION_NAME)
                    item.setOnClickListener { _ ->
                        try {
                            (it.thisObject as Context).startActivity<SettingActivity>()
                        } catch (e: Throwable) {
                            log.e(e)
                        }
                    }
                    item.setOnLongClickListener { _ ->
                        log.d("OnLongClick")
                        true
                    }

                    (itemRef.parent as ViewGroup).let { list -> if (list.childCount != 1) list else list.parent as ViewGroup }
                        .addView(item, 0, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                } catch (e: Throwable) {
                    log.e(e)
                }
            }
            return true
        } catch (e: Throwable) {
            log.e(e)
            return false
        }
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun setEnabled(z: Boolean) {
    }
}