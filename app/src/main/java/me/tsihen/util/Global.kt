package me.tsihen.util

import android.os.Environment

val defaultDirPath = Environment.getExternalStorageDirectory().path + "/QQ工具"
const val PACKAGE_QQ = "com.tencent.mobileqq"
const val PACKAGE_SELF = "me.tsihen.qtools"

const val TARGET_INTENT = "QTools_target_intent"

const val SCRIPT_DO = 1 shl 0