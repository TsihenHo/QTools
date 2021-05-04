package me.tsihen.qtools.script

import bsh.BshMethod
import bsh.Interpreter
import bsh.UtilEvalError
import de.robv.android.xposed.XposedHelpers.newInstance
import me.tsihen.qtools.BuildConfig
import me.tsihen.qtools.script.ScriptApi.getNickname
import me.tsihen.qtools.script.ScriptManager.scriptClassLoader
import me.tsihen.util.getAccountUin
import me.tsihen.util.getQQApplication
import me.tsihen.util.log
import java.io.File
import java.io.InputStreamReader

class Script(val file: File) {
    val engine = Interpreter()
    private var init = false
    var enable = false

    fun doInit() {
        val uin = getAccountUin()
        engine["mQQ"] = uin
        engine["context"] = getQQApplication()
        engine["mName"] = getNickname(uin, uin)
        if (init) return
        engine.setClassLoader(scriptClassLoader)
        engine.nameSpace.setMethod(
            "load",
            newInstance(
                BshMethod::class.java,
                Interpreter::class.java.getMethod("source", String::class.java),
                engine
            ) as BshMethod
        )
        engine.nameSpace.setMethod(
            "Toast",
            newInstance(
                BshMethod::class.java,
                ScriptApi::class.java.getMethod("toast", String::class.java),
                null
            ) as BshMethod
        )
        engine.eval("import static me.tsihen.qtools.script.ScriptApi.*;")
        try {
            engine.eval(file.reader())
        } catch (e: Throwable) {
            log.e(e)
            return
        }
        init = true
    }

    fun eval(code: String) {
        if (!init) doInit()
        engine.eval(code)
    }

    fun eval(reader: InputStreamReader) {
        if (!init) doInit()
        engine.eval(reader)
    }

    fun onMsg(data: MessageData) {
        if (!init) return
        try {
            if (data.type == 1)
                engine.nameSpace.getMethod("onMsg", arrayOf(Any::class.java))
                    ?.invoke(arrayOf(data), engine)
            else if (data.type == 2)
                engine.nameSpace.getMethod("onPicMsg", arrayOf(Any::class.java))?.invoke(
                    arrayOf(data),
                    engine
                )
            else if (data.type == 3 || data.type == 4)
                engine.nameSpace.getMethod("onCardMsg", arrayOf(Any::class.java))?.invoke(
                    arrayOf(data),
                    engine
                )
            else if (data.type == 5)
                engine.nameSpace.getMethod("onReplyMsg", arrayOf(Any::class.java))?.invoke(
                    arrayOf(data),
                    engine
                )
            else if (data.type == 6)
                engine.nameSpace.getMethod("onMixedMsg", arrayOf(Any::class.java))?.invoke(
                    arrayOf(data),
                    engine
                )
            else if (data.type == 0)
                engine.nameSpace.getMethod("onRawMsg", arrayOf(Any::class.java))?.invoke(
                    arrayOf(data),
                    engine
                )
        } catch (e: Throwable) {
            log.e(e)
        }
    }

    fun onJoin(groupUin: String, person: String) {
        if (!init) return
        try {
            engine.nameSpace.getMethod("onJoin", arrayOf(String::class.java, String::class.java))
                ?.invoke(
                    arrayOf(groupUin, person),
                    engine
                )
        } catch (e: Throwable) {
            log.e(e)
        }
    }
}