package me.tsihen.util

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

inline fun Method.before(crossinline action: (XC_MethodHook.MethodHookParam) -> Unit) {
    XposedBridge.hookMethod(this, object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            action(param)
        }
    })
}

inline fun Method.after(crossinline action: (XC_MethodHook.MethodHookParam) -> Unit) {
    XposedBridge.hookMethod(this, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            action(param)
        }
    })
}