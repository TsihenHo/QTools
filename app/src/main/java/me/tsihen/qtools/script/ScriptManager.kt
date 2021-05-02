package me.tsihen.qtools.script

import android.os.Handler
import android.os.Looper
import bsh.EvalError
import dalvik.system.PathClassLoader
import me.tsihen.config.ConfigManager.Companion.config
import me.tsihen.qtools.MainHook
import me.tsihen.util.defaultDirPath
import me.tsihen.util.getApkPath
import me.tsihen.util.getQQApplicationNonNull
import java.io.File

object ScriptManager {
    private val scriptMap = mutableMapOf<File, Script>()
    val scripts
        get() = scriptMap.values
    private val scriptThread = object : Thread() {
        lateinit var mH: Handler

        override fun run() {
            super.run()
            Looper.prepare()
            mH = Handler(Looper.myLooper())
            Looper.loop()
        }
    }

    val scriptDirPath = "$defaultDirPath${File.separator}script"
    val scriptClassLoader = object : ClassLoader() {
        val bugClassLoader: ClassLoader by lazy {
            try {
                PathClassLoader(
                    getApkPath(getQQApplicationNonNull(), "com.bug.zqq"),
                    getSystemClassLoader()
                )
            } catch (e: Exception) {
                getSystemClassLoader()
            }
        }

        override fun findClass(name: String?): Class<*> {
            return try {
                MainHook::class.java.classLoader!!.loadClass(name)
            } catch (e: ClassNotFoundException) {
                bugClassLoader.loadClass(name)
            }
        }
    }

    private fun runAndCatchScript(action: () -> Unit) {
        try {
            action()
        } catch (e: EvalError) {
            throw IllegalArgumentException(
                "脚本语法错误：source = ${e.errorSourceFile}, line = ${e.errorLineNumber}," +
                        " text = ${e.errorText}, stackTrace = ${e.scriptStackTrace}"
            ).initCause(e)
        }
    }

    fun loadScriptFromFile(file: File) {
        if (!file.isFile) throw IllegalArgumentException("脚本不能是文件夹/不存在/无法读取")
        val script = Script(file)
        scriptMap[file] = script
        script.enable = config.getOr("script_enable__${file.name}", false)
    }

    fun doInit() {
        try {
            scriptThread.start()
        } catch (e: IllegalThreadStateException) {
        }
        val dir = File(scriptDirPath)
        if (dir.isDirectory) {
            writeDemo()
            dir.listFiles()?.forEach {
                if (config["script_enable_${it.name}"] != null || it.name.endsWith(".java")) {
                    loadScriptFromFile(it)
                    val s = scriptMap[it]
                    if (s?.enable == true) {
                        scriptThread.mH.post { s.doInit() }
//                        s.doInit()
                    }
                }
            }
        } else {
            if (dir.exists()) dir.delete()
            dir.mkdirs()
            doInit()
        }
    }

    private fun writeDemo() {
        val demo = File("$scriptDirPath${File.separator}示例脚本.java")
        if (!demo.exists()) demo.createNewFile()
        demo.writeText(
            MainHook::class.java.classLoader!!.getResourceAsStream("assets/demo.java").reader()
                .readText()
        )
    }

    fun changeEnable(script: Script) {
        config["script_enable__${script.file.name}"] = !script.enable
        script.enable = !script.enable
    }


    fun onMsg(data: MessageData) {
        scriptThread.mH.post {
            scripts.forEach {
                if (!it.enable) return@forEach
                it.onMsg(data)
            }
        }
    }

    fun onJoin(groupUin: String, person: String) {
        scriptThread.mH.post {
            scripts.forEach {
                if (!it.enable) return@forEach
                it.onJoin(groupUin, person)
            }
        }
    }
}