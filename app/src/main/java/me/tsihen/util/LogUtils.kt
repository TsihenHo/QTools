package me.tsihen.util

import android.os.Build
import android.os.Process
import android.util.Log
import de.robv.android.xposed.XposedBridge
import me.tsihen.qtools.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.StringReader
import java.util.*

class LogUtils(val file: File) {
    private val enableXposed = try {
        XposedBridge.log("FAKE")
        true
    } catch (ignored: Throwable) {
        false
    }

    init {
        File(defaultDirPath).mkdirs()
        if (!file.exists()) file.createNewFile()
    }

    private fun printToXposed(s: String) {
        if (enableXposed) XposedBridge.log("QQ工具/$s")
    }

    @JvmOverloads
    fun e(t: Throwable, noFile: Boolean = false) {
        e(t.getStackTraceWithoutSystemAndMsg(), noFile)
    }

    @JvmOverloads
    fun e(s: String, noFile: Boolean = false) {
        printToXposed(s)
        Log.e("QTDump", s)
        if (noFile) return
        val nowTime = Date().toString()
        file.appendText("[$nowTime ${Process.myPid()}]E/$s\n")
    }

    @JvmOverloads
    fun d(s: String, noFile: Boolean = false) {
        if (!BuildConfig.DEBUG) return
        printToXposed(s)
        Log.d("QTDump", s)
        if (!noFile) {
            val nowTime = Date().toString()
            file.appendText("[$nowTime ${Process.myPid()}]D/$s\n")
        }
    }

    fun i(s: String) {
        printToXposed(s)
        Log.i("QTDump", s)
        val nowTime = Date().toString()
        file.appendText("[$nowTime ${Process.myPid()}]I/$s\n")
    }

    fun w(s: String) {
        printToXposed(s)
        Log.w("QTDump", s)
        val nowTime = Date().toString()
        file.appendText("[$nowTime ${Process.myPid()}]W/$s\n")
    }
}

fun Throwable.getStackTraceWithoutSystemAndMsg(): String {
    if (BuildConfig.DEBUG) return Log.getStackTraceString(this)
    return try {
        val str = Log.getStackTraceString(this)
        val reader = BufferedReader(StringReader(str))
        val res = java.lang.StringBuilder(javaClass.simpleName)
        res.append(":$message")
        res.append('\n')
        var count = 0
        if (this !is Error) {
            var r = reader.readLine()
            while (r != null) {
                if (r.contains(Regex("\\s*at android\\."))
                    || r.contains(Regex("\\s*at com\\.android\\."))
                    || r.contains(Regex("\\s*at java\\."))
                ) {
                    continue
                }
                count += 1
                if (count >= 40) {
                    res.append("推测：栈溢出，停止\n")
                    break
                }
                res.append("$r\n")
                r = reader.readLine()
            }
            res.toString()
        } else str
    } catch (e: Exception) {
        log.e(Log.getStackTraceString(e))
        Log.getStackTraceString(this)
    }
}

val log: LogUtils = LogUtils(File("$defaultDirPath/log.log"))