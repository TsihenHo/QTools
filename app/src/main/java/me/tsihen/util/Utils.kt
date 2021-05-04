package me.tsihen.util

import android.app.Activity
import android.content.Context
import java.io.File
import java.util.*

fun getApkPath(ctx: Context, packageName: String): String =
    ctx.createPackageContext(
        packageName,
        Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
    ).packageResourcePath

fun getCurrActivity(): Activity? {
    try {
        val clz = Class.forName("android.app.ActivityThread")
        val currThreadField = clz.getDeclaredField("sCurrentActivityThread")
        currThreadField.isAccessible = true
        val activitiesField = clz.getDeclaredField("mActivities")
        activitiesField.isAccessible = true
        (activitiesField[currThreadField[null]] as Map<*, *>).values.forEach {
            if (it == null) return@forEach
            if (!getObject(it, "paused", Boolean::class.java)!!) return getObject(
                it,
                "activity",
                Activity::class.java
            )
        }
    } catch (e: Exception) {
        log.e(e)
    }
    return null
}

/**
 * 获取精确到秒的时间戳
 * @return
 */
fun getSecondTimestamp(date: Date?): Int {
    if (null == date) {
        return 0
    }
    val timestamp = java.lang.String.valueOf(date.time)
    val length = timestamp.length
    return if (length > 3) {
        Integer.valueOf(timestamp.substring(0, length - 3))
    } else {
        0
    }
}

