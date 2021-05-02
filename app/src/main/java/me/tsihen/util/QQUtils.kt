package me.tsihen.util

import android.app.Application
import androidx.annotation.NonNull
import de.robv.android.xposed.XposedHelpers
import me.tsihen.treflex.filter.FieldFilter
import me.tsihen.treflex.getFirstField
import java.lang.NullPointerException

private val qqApplicationField by lazy {
    val clz: Class<*> = getClass("com.tencent.common.app.BaseApplicationImpl")
    try {
        clz.getDeclaredField("sApplication")
    } catch (ignored: NoSuchFieldException) {
        getFirstField(clz, true, FieldFilter(name = "a", type = clz))
    }
}

var qqAppInterface: Any? = null
    @JvmName("setQQAppInterface")
    set
    @JvmName("getQQAppInterface")
    @NonNull
    get() {
        if (field == null) return getAppRuntime()
        return field
    }

fun getQQApplication(): Application? {
    return try {
        qqApplicationField[null] as Application?
    } catch (e: java.lang.Exception) {
        log.e(e, true)
        throw RuntimeException("FATAL: QQUtils.getApplication() failure!").initCause(e)
    }
}

fun getQQApplicationNonNull(): Application {
    return getQQApplication() ?: throw NullPointerException("QQApplication is null.")
}

fun getAppRuntime(): Any {
    val ctx = getQQApplicationNonNull()
    return ctx.callVirtualMethod("getRuntime")
        ?: throw NullPointerException("QQUtils : GetAppRuntime : Runtime is null.")
}

fun getLongAccountUin(): Long = getAppRuntime().callVirtualMethod("getLongAccountUin") as Long
fun getAccountUin(): String =
    XposedHelpers.callMethod(getAppRuntime(), "getCurrentAccountUin").toString()