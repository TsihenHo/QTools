package me.tsihen.qtools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import dalvik.system.PathClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.tsihen.qtools.hook.GetMsgHook
import me.tsihen.qtools.hook.SettingEntryHook
import me.tsihen.qtools.script.ScriptManager
import me.tsihen.treflex.getClass
import me.tsihen.util.*
import java.io.File


class MainHook : IXposedHookLoadPackage {
    companion object {
        lateinit var instance: MainHook
    }

    private var firstInit = false
    private var secondInit = false
    private var thirdInit = false

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        instance = this
        if (lpparam.packageName != PACKAGE_QQ) return
        doHook(lpparam)
    }

    private fun doHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (firstInit) return
        val classLoader = lpparam.classLoader
//        getClass(
//            classLoader,
//            "$PACKAGE_QQ.qfix.QFixApplication"
//        ).getDeclaredMethod("attachBaseContext", Context::class.java).after {
//            if (secondInit) return@after
//            val ctx = it.thisObject as Context
//            loader = classLoader
//            performHook(ctx)
//            secondInit = true
//        }
        getClass(
            classLoader,
            "$PACKAGE_QQ.activity.SplashActivity"
        ).getDeclaredMethod("doOnCreate", Bundle::class.java).before {
            if (secondInit) return@before
            val ctx = it.thisObject as Activity
            loader = classLoader
            HostApp.init(ctx.application)
            performHook(ctx)
            secondInit = true
        }
        firstInit = true
    }

    private fun performHook(ctx: Context) {
        if (thirdInit) return
        try {
            inject(ctx)
            XposedHelpers.findAndHookMethod(
                getClass("$PACKAGE_QQ.app.QQAppInterface"),
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        super.afterHookedMethod(param)
                        qqAppInterface = param.thisObject
                    }
                }
            )

            SettingEntryHook.init()
            GetMsgHook.init()

            ScriptManager.doInit()
        } catch (e: Throwable) {
            log.e(e)
            return
        }
        thirdInit = true
    }

    private fun inject(ctx: Context) {
        // First, inject res
        injectModuleRes(ctx)
        // Second, inject activity
        injectActivity(ctx)
    }

    private var modulePath = ""
    internal fun injectModuleRes(context: Context) {
        try {
            context.resources.getString(R.string.app_name)
            return
        } catch (ignored: Exception) {
        }
        if (modulePath.isEmpty() || modulePath.isBlank()) {
            val pathClassLoader = MainHook::class.java.classLoader as PathClassLoader
            val pathList = XposedHelpers.getObjectField(pathClassLoader, "pathList")
            val dexElements = XposedHelpers.getObjectField(pathList, "dexElements") as Array<*>
            dexElements.forEach {
                val file: File? =
                    XposedHelpers.getObjectField(it, "path") as File?
                        ?: XposedHelpers.getObjectField(it, "zip") as File?
                        ?: XposedHelpers.getObjectField(it, "file") as File?
                if (file != null && file.isFile) {
                    val path = file.path
                    if (modulePath.isBlank()
                        || modulePath.isEmpty()
                        || !modulePath.contains(PACKAGE_SELF)
                    ) modulePath = path
                }
            }
            if (modulePath.isBlank() || modulePath.isEmpty()) throw RuntimeException("无法获取模块资源，$pathClassLoader")
        }
        val assetsManager = context.resources.assets
        XposedHelpers.callMethod(assetsManager, "addAssetPath", modulePath)
    }

    private fun injectActivity(ctx: Context) {
        initForStubActivity(ctx)
    }
}