package me.tsihen.qtools.hook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import me.tsihen.config.ClassConfig.messageHandlerUtilsClz
import me.tsihen.config.ClassConfig.messageRecordClz
import me.tsihen.config.ClassConfig.qqAppInterfaceClz
import me.tsihen.qtools.script.MessageData.Companion.getMessage
import me.tsihen.qtools.script.ScriptManager
import me.tsihen.util.PACKAGE_QQ
import me.tsihen.util.loader
import me.tsihen.util.log
import me.tsihen.xposed.AbsHook

object GetMsgHook : AbsHook() {
    private val msgDone = mutableListOf<Long>()

    override fun doInit(): Boolean {
        try {
            val getMsgMethod = messageHandlerUtilsClz.getDeclaredMethod(
                "a",
                qqAppInterfaceClz,
                messageRecordClz,
                true.javaClass
            )
            XposedBridge.hookMethod(getMsgMethod, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        val p = getMessage(param.args[0], param.args[1])
                        val type = param.args[1].javaClass
                        log.d("GetMsgHook : type is ${type.simpleName}.")
                        // 仅仅接受用户发送的部分类型消息
//                        if (
//                            !(loader.loadClass("$PACKAGE_QQ.data.MessageForText"))
//                                .isAssignableFrom(type) &&
//                            !(loader.loadClass("$PACKAGE_QQ.data.MessageForLongMsg"))
//                                .isAssignableFrom(type) &&
//                            !(loader.loadClass("$PACKAGE_QQ.data.MessageForPic"))
//                                .isAssignableFrom(type) &&
//                            !(loader.loadClass("$PACKAGE_QQ.data.MessageForStructing"))
//                                .isAssignableFrom(type) &&
//                            !(loader.loadClass("$PACKAGE_QQ.data.MessageForArkApp"))
//                                .isAssignableFrom(type) &&
//                            !(loader.loadClass("$PACKAGE_QQ.data.MessageForReplyText"))
//                                .isAssignableFrom(type) &&
//                            !(loader.loadClass("$PACKAGE_QQ.data.MessageForMixedMsg"))
//                                .isAssignableFrom(type)
//                        ) return

                        // 防止重复
                        if (msgDone.contains(p.id)) return
                        msgDone.add(p.id)
                        // 控制内存
                        if (msgDone.size > 10) msgDone.removeAt(0)

                        ScriptManager.onMsg(p)
                    } catch (e: Exception) {
                        log.e(e)
                    }
                }
            })
        } catch (e: Exception) {
            log.e(e)
            return false
        }
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun setEnabled(z: Boolean) {
    }
}