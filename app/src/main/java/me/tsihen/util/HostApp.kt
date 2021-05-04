package me.tsihen.util

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat

object HostApp {
    lateinit var application: Application
    val isQQ: Boolean
        @JvmName("isQQ")
        get() = application.packageName.contains("mobileqq")
    val isTIM: Boolean
        @JvmName("isTIM")
        get() = application.packageName.contains("tim")
    val packageInfo: PackageInfo
        get() = application.packageManager.getPackageInfo(
            application.packageName,
            PackageManager.GET_META_DATA
        )
    val versionName: String
        get() = packageInfo.versionName
    val versionCode: Long
        get() = PackageInfoCompat.getLongVersionCode(packageInfo)

    fun init(application: Application) {
        this.application = application
    }
}